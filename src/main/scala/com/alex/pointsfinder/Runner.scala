package com.alex.pointsfinder

import java.io.File
import java.util.Date

import org.deeplearning4j.optimize.listeners.{CollectScoresIterationListener, ScoreIterationListener}


object Runner extends App {
  // Change the constants
  val TRAIN_PATH = "training.csv"
  val TEST_PATH = "test.csv"
  val LEARNED_WEIGHTS_EXPORT_PATH = "weights"
  val LEARNING_STATS_EXPORT_PATH = "learning-stats.csv"

  // Read all training data
  val trainSet = ImageWithPoints.fromCSV(TRAIN_PATH)
  println(s"Whole train set size: ${trainSet.length}")

  // Leave only those example that have eye centers defined
  val eyeCentersTrainSet = trainSet.filter(_.pointsDefined(LEFT_EYE_CENTER, RIGHT_EYE_CENTER))
  println(s"Eye centers train set size: ${eyeCentersTrainSet.length}")

  // Increase training set size by adding same but mirrored examples and leave only eye center points
  val lessons = (eyeCentersTrainSet ++ eyeCentersTrainSet.map(_.mirrored))
    .map(_.withOnlyPointsOfTypes(LEFT_EYE_CENTER, RIGHT_EYE_CENTER))
  println(s"Lessons for eye centers size size: ${lessons.length}")

  // Create finder for eye centers
  val finder = PointsFinder(LEFT_EYE_CENTER, RIGHT_EYE_CENTER)

  // Set up learning listener to analyze learning process
  val learningListener = new CollectScoresIterationListener()
  finder.model.setListeners(new ScoreIterationListener(100), learningListener)

  // Start 5 epochs learning and save learned weights
  val numEpochs = 5
  println(s"Start learning on $numEpochs epochs at ${new Date()} ")
  finder.train(lessons, numEpochs)
  finder.saveKnowledge(LEARNED_WEIGHTS_EXPORT_PATH)
  println(s"End learning at ${new Date()}")

  // Export learning stats as CSV
  learningListener.exportScores(new File(LEARNING_STATS_EXPORT_PATH), ",")

  // Read test set
  val testSet = Image.fromCSV(TEST_PATH)

  // Process each 25th image and its mirrored version
  for (i <- 0 to testSet.length by 25) {
    ImageViewer.view(finder.find(testSet(i)))
    ImageViewer.view(finder.find(testSet(i).mirrored))
  }
}