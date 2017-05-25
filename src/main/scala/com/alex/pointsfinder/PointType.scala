package com.alex.pointsfinder


sealed class PointType(index: Int) {
  def apply() = index
}

object LEFT_EYE_CENTER extends PointType(0)

object RIGHT_EYE_CENTER extends PointType(1)

object LEFT_EYE_INNER_CORNER extends PointType(2)

object LEFT_EYE_OUTER_CORNER extends PointType(3)

object RIGHT_EYE_INNER_CORNER extends PointType(4)

object RIGHT_EYE_OUTER_CORNER extends PointType(5)

object LEFT_EYEBROW_INNER_END extends PointType(6)

object LEFT_EYEBROW_OUTER_END extends PointType(7)

object RIGHT_EYEBROW_INNER_END extends PointType(8)

object RIGHT_EYEBROW_OUTER_END extends PointType(9)

object NOSE_TIP extends PointType(10)

object MOUTH_LEFT_CORNER extends PointType(11)

object MOUTH_RIGHT_CORNER extends PointType(12)

object MOUTH_CENTER_TOP_LIP extends PointType(13)

object MOUTH_CENTER_BOTTOM_LIP extends PointType(14)