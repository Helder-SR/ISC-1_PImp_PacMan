package Frontend

import java.awt.Color
import java.io.{File, FileInputStream}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

object AssetManager {
  private val SPRITE_SIZE = 32
  private val IMAGES_DIR = "res/"

  def loadSprites() : Array[Sprite] = {
    val directory = new File(IMAGES_DIR)

    if (directory.exists && directory.isDirectory) {
      val files = directory.listFiles
      val validImages = files.filter(f => f.isFile && f.getName.endsWith(".png"))
      val sprites = validImages.flatMap(image => {
        try {
          Some(loadSprite(image.getPath))
        } catch {
          case e: Exception =>
            println(s"Error with image: ${image.getName}: $e")
            None
        }
      })

      return sprites

    } else {
      println(s"Error with images directory location")
      return Array.empty[Sprite]
    }
  }

  def loadSprite(filePath: String): Sprite = {
    var imgBuffer: BufferedImage = null
    var w = 0
    var h = 0

    try {
      imgBuffer = ImageIO.read(new File(filePath))
      w = imgBuffer.getWidth
      h = imgBuffer.getHeight
    } catch {
      case e: Exception =>
        throw new Exception(s"Could not load image at location: $filePath")
    }

    if(w != SPRITE_SIZE || h != SPRITE_SIZE){
      throw new Exception(s"The image at $filePath has incorrect size: Image is ${w}x${h}")
    }

    val pixels: Array[Array[Color]] = getPixelsColor(imgBuffer)
    val sprite: Sprite = new Sprite(SPRITE_SIZE, pixels)

    return sprite
  }

  private def getPixelsColor(imgBuffer: BufferedImage): Array[Array[Color]] = {
    val values = Array.ofDim[Color](SPRITE_SIZE, SPRITE_SIZE)
    for (i <- 0 until SPRITE_SIZE) {
      for (j <- 0 until SPRITE_SIZE) {
        values(i)(j) = new Color(imgBuffer.getRGB(i, j), true)  // true to handle transparency
      }
    }
    return values
  }
}
