package com.alex.pointsfinder

import java.io.File
import java.nio.file.{Files, Paths}

import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers._
import org.deeplearning4j.nn.conf.{GradientNormalization, LearningRatePolicy, NeuralNetConfiguration, Updater}
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.nn.weights.WeightInit
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction
import org.nd4s.Implicits._


class PointsFinder private(val model: MultiLayerNetwork, types: Seq[PointType]) {
  assert(types.nonEmpty, "At least 1 point type should be provided")

  import PointsFinder._

  @Deprecated
  def find(image: Image): ImageWithPoints = {
    val input = Nd4j.create(image.contentAsFloats)
    val output = model.output(input)
    val indices = types.map(_.apply())
    var outputIndex = 0
    val filteredPoints = (0 to ImageWithPoints.FEATURES_COUNT) map { i =>
      if (indices.contains(i)) {
        val point = FacialKeyPoint(output.getFloat(outputIndex) * 96, output.getFloat(outputIndex + 1) * 96)
        outputIndex = outputIndex + 2
        Some(point)
      }
      else
        None
    }
    ImageWithPoints(image, filteredPoints)
  }

  def findPoints(image: Image): Map[PointType, FacialKeyPoint] = {
    val input = Nd4j.create(image.contentAsFloats)
    val output = model.output(input)
    val typesToPoints = for (i <- 0 until output.length() by 2)
      yield types(i / 2) -> FacialKeyPoint(output.getFloat(i) * 96, output.getFloat(i + 1) * 96)
    typesToPoints.toMap
  }

  def train(lessons: Seq[ImageWithPoints], numEpochs: Int) = {
    val dataSet = createDataSet(lessons)

    val dataSetIterator = new ListDataSetIterator(dataSet.asList(), 10)
    model.fit(new MultipleEpochsIterator(numEpochs, dataSetIterator))
  }

  def saveKnowledge(path: String) = PointsFinder.saveNet(model, path)
}

object PointsFinder {

  def apply(pointTypes: PointType*) =
    new PointsFinder(createNet(pointTypes.length * 2), pointTypes)

  def createNet(nOut: Int) = {
    val model = secondModel(nOut)
    model.init()
    model.setListeners(new ScoreIterationListener(100))
    model
  }

  def apply(path: String, pointTypes: PointType*) = new PointsFinder(
    model = if (Files.exists(Paths.get(path)))
      readNet(path)
    else
      createNet(pointTypes.length * 2),
    types = pointTypes
  )

  private def readNet(path: String) = {
    val locationToSave = new File(path)
    val model = ModelSerializer.restoreMultiLayerNetwork(locationToSave)
    model.setListeners(new ScoreIterationListener(100))
    model
  }

  private def saveNet(model: MultiLayerNetwork, path: String) = {
    val locationToSave = new File(path)
    val saveUpdater = true
    ModelSerializer.writeModel(model, locationToSave, saveUpdater)
  }

  private def secondModel(nOut: Int) = {
    val conf = new NeuralNetConfiguration.Builder()
      .seed(123)
      .weightInit(WeightInit.UNIFORM)
      .activation(Activation.RELU) //
      .updater(Updater.NESTEROVS)
      .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .learningRate(0.02)
      .biasLearningRate(0.02)
      .learningRateDecayPolicy(LearningRatePolicy.Step)
      .lrPolicyDecayRate(0.1)
      .lrPolicySteps(      100000)
      .regularization(true)
      .l2(5 * 1e-4)
      .momentum(0.9)
      .list()

      .layer(0, new ConvolutionLayer.Builder(3, 3)
        .nIn(1)
        .stride(2, 2)
        .nOut(32)
        .activation(Activation.RELU)
        .build())

      .layer(1, new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(1, 1)
        .build())

      .layer(2, new ConvolutionLayer.Builder(2, 2)
        .stride(1, 1)
        .nOut(64)
        .activation(Activation.RELU)
        .build())

      .layer(3, new SubsamplingLayer.Builder(PoolingType.MAX)
        .kernelSize(2, 2)
        .stride(1, 1)
        .build())

      .layer(4, new DenseLayer.Builder()
        .nOut(500)
        .build())

      .layer(5, new DenseLayer.Builder()
        .nIn(500)
        .nOut(200)
        .build())

      .layer(6, new OutputLayer.Builder(LossFunction.MSE)
        .nIn(200)
        .nOut(nOut)
        .activation(Activation.IDENTITY)
        .build())

      .backprop(true)
      .pretrain(false)
      .setInputType(InputType.convolutionalFlat(96, 96, 1))
      .build()

    new MultiLayerNetwork(conf)
  }

  private def createDataSet(lessons: Seq[ImageWithPoints]) = {
    val images = lessons.map(_.image.contentAsFloats).toArray
    val points = lessons.map(_.points.flatten.flatMap(p => Seq(p.x / 96, p.y / 96)).toArray).toArray

    new DataSet(images.toNDArray, points.toNDArray)
  }
}