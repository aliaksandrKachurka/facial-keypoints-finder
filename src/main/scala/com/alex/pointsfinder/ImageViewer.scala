package com.alex.pointsfinder

import java.awt.image.BufferedImage
import javax.swing.{ImageIcon, JOptionPane}

object ImageViewer {

  def view(image: Image): Unit = {
    val bImg = createBImg(image)
    showImg(bImg)
  }

  def view(imageWithPoints: ImageWithPoints) = {
    val bImg = createBImg(imageWithPoints.image)
    addPointsToImage(bImg, imageWithPoints.points.flatten)
    showImg(bImg)
  }

  private def addPointsToImage(bImg: BufferedImage, points: Seq[FacialKeyPoint]) = {
    for (p <- points) {
      bImg.setRGB(p.x.toInt, p.y.toInt, p.color.getRGB)
    }
  }

  private def createBImg(img: Image) = {
    val w = img.width
    val h = img.height

    val bImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
    val rgbs = img.content.map(toRGB)
    bImg.setRGB(0, 0, w, h, rgbs, 0, 0)
    for (y <- 0 until h; x <- 0 until w) {
      val curColor = img.content(y * h + x)
      bImg.setRGB(x, y, toRGB(curColor))
    }
    bImg
  }

  private def toRGB(i: Int) = 65536 * i + 256 * i + i

  private def showImg(bImg: BufferedImage) = {
    val scaled = resize(bImg, 768, 768)
    JOptionPane.showMessageDialog(null, null, "Pic", 2, new ImageIcon(scaled))
  }

  private def resize(img: BufferedImage, newH: Int, newW: Int) = {
    val tmp = img.getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH)
    val dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB)
    val g2d = dimg.createGraphics()
    g2d.drawImage(tmp, 0, 0, null)
    g2d.dispose()
    dimg
  }
}