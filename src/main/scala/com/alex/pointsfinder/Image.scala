package com.alex.pointsfinder

import scala.io.Source

class Image(val height: Int, val width: Int, val content: Array[Int]) {
  assert(height * width == content.length, "Invalid content array size")

  def contentAsFloats: Array[Float] = content.map(i => i.toFloat / Image.MAX_COLOR)

  def contentAs2D: Array[Array[Int]] = Array.tabulate(96, 96)((i, j) => content(96 * i + j))

  def contentAs2DFloats: Array[Array[Float]] = {
    contentAs2D.map(_.map(_.toFloat / Image.MAX_COLOR))
  }

  def mirrored: Image = {
    val mirroredContent = contentAs2D.flatMap(_.reverse)
    Image(mirroredContent)
  }
}

object Image {
  private val MAX_COLOR = 256

  def apply(content: Array[Int]) = {
    val sideSize = math.sqrt(content.length).toInt
    assert(sideSize * sideSize == content.length, "Image is not of square shape")
    new Image(sideSize, sideSize, content)
  }

  def apply(str: String): Image = {
    val ints = str.split(" ").map(_.toInt)
    Image(ints)
  }

  def fromFile(path: String): Image = apply(Source.fromFile(path).mkString)

  def fromCSV(path: String): Array[Image] = {
    val lines = Source.fromFile(path).getLines().toSeq
    lines.drop(1).map { line =>
      val values = line.split(",")
      val imgData = values.last.split(" ").map(_.toInt)
      Image(imgData)
    }.toArray
  }
}
