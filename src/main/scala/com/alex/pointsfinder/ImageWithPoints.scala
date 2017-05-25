package com.alex.pointsfinder

import java.awt.Color

import scala.io.Source

case class FacialKeyPoint(x: Float, y: Float, color: Color = Color.RED)

case class ImageWithPoints(image: Image, points: Seq[Option[FacialKeyPoint]]) {
  import ImageWithPoints.FEATURES_COUNT

  val allPointsDefined: Boolean = !points.contains(None)

  def pointsDefined(types: PointType*) = types.forall(t => points(t()).isDefined)

  def withOnlyPointsOfTypes(types: PointType*) = {
    val neededIndices = types.map(_.apply())
    val filteredPoints = (0 to FEATURES_COUNT) map { i =>
      if (neededIndices.contains(i))
        points(i)
      else
        None
    }
    this.copy(points = filteredPoints)
  }

  def mirrored = new ImageWithPoints(image.mirrored, mirroredPoints)

  private def mirroredPoints = Seq(
    mirroredPoint(points(RIGHT_EYE_CENTER())), // 0
    mirroredPoint(points(LEFT_EYE_CENTER())), // 1
    mirroredPoint(points(RIGHT_EYE_INNER_CORNER())), // 2
    mirroredPoint(points(RIGHT_EYE_OUTER_CORNER())), // 3
    mirroredPoint(points(LEFT_EYE_INNER_CORNER())), // 4
    mirroredPoint(points(LEFT_EYE_OUTER_CORNER())), // 5
    mirroredPoint(points(RIGHT_EYEBROW_INNER_END())), // 6
    mirroredPoint(points(RIGHT_EYEBROW_OUTER_END())), // 7
    mirroredPoint(points(LEFT_EYEBROW_INNER_END())), // 8
    mirroredPoint(points(LEFT_EYEBROW_OUTER_END())), // 9
    mirroredPoint(points(NOSE_TIP())), // 10
    mirroredPoint(points(MOUTH_RIGHT_CORNER())), // 11
    mirroredPoint(points(MOUTH_LEFT_CORNER())), // 12
    mirroredPoint(points(MOUTH_CENTER_TOP_LIP())), // 13
    mirroredPoint(points(MOUTH_CENTER_BOTTOM_LIP())) // 14
  )

  private def mirroredPoint(point: Option[FacialKeyPoint]) = {
    point.map(p => p.copy(x = image.width - p.x))
  }
}

object ImageWithPoints {
  val FEATURES_COUNT = 15

  def fromCSV(path: String): Array[ImageWithPoints] = {
    val lines = Source.fromFile(path).getLines().toSeq
    lines.drop(1).map { line =>
      val values = line.split(",")
      val imgData = values.last.split(" ").map(_.toInt)
      val pCoords = values.dropRight(1)
      val points = pCoords.sliding(2, 2).map { pair =>
        // Some features missed
        if (pair.head.isEmpty)
          None
        else
          Some(FacialKeyPoint(pair(0).toFloat, pair(1).toFloat))
      }
      ImageWithPoints(Image(imgData), points.toIndexedSeq)
    }.toArray
  }
}
