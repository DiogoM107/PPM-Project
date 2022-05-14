package main.scala.ppm

import javafx.scene.{Group, Node}
import javafx.scene.paint.PhongMaterial
import javafx.scene.shape.{Box, DrawMode, Shape3D}
import main.scala.ppm.InitSubScene.{blueMaterial, camVolume, whiteMaterial}
import main.scala.ppm.PureLayer.{MAX_SCALE, Placement, Section, getAllShapesFromRoot}

import java.io.{File, PrintWriter}

object ImpureLayer {

  val OUTPUT_FILE = "output.txt"

  /**
   * Funcao usada para criar uma particao
   *
   * @param placement - o placement
   * @return - a particao
   */
  // Cria um cubo com o placement enviado como parametro
  def createShapeCube(placement: Placement): Box = {
    val ((x, y, z), size) = placement

    val box = new Box(size, size, size)
    box.setTranslateX(size / 2 + x)
    box.setTranslateY(size / 2 + y)
    box.setTranslateZ(size / 2 + z)

    // Aqui temos que ver se a particao está dentro do campo de visão do CamVolume, para definirmos a cor
    if (box.asInstanceOf[Shape3D].getBoundsInParent.intersects(camVolume.asInstanceOf[Shape3D].getBoundsInParent)) {
      box.setMaterial(blueMaterial)
    }
    else {
      box.setMaterial(whiteMaterial)
    }
    box.setDrawMode(DrawMode.LINE)
    box
  }

  // Serve para testes
  def printOctTree[A](octree: Octree[A]): Unit = {
    octree match {
      case OcEmpty => ""
      case OcNode(coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) => {
        printOctTree(up_00)
        printOctTree(up_01)
        printOctTree(up_10)
        printOctTree(up_11)
        printOctTree(down_00)
        printOctTree(down_01)
        printOctTree(down_10)
        printOctTree(down_11)
      }
      case OcLeaf(section) => {
        printShapesList(section.asInstanceOf[Section]._2)
      }
    }
  }

  //Serve para testes
  def printShapesList(shapes: List[Node]): Unit = {
    shapes match {
      case Nil => ""
      case x :: tail => {
        printShape(x.asInstanceOf[Shape3D])
        printShapesList(tail)
      }
    }
  }

  // Serve para testes
  def printShape(shape: Shape3D): Unit = {
    println(s"Class: ${shape.getClass}, ${shape.getMaterial}, ${shape.getTranslateX}, ${shape.getTranslateY}, ${shape.getTranslateZ}, ${shape.getScaleX}, ${shape.getScaleY}, ${shape.getScaleZ}")
  }

  /**
   * Funcao usada para escrever no ficheiro de output
   * @param file - o nome do ficheiro de output
   * @param root - o worldRoot
   * @param oct - a octree
   */
  def writeToFile(file: String, oct: Octree[Placement], root: Group): Unit = {
    val writer = new PrintWriter(new File(file))
    //Aqui vai buscar o valor do scale da octree final para escrever na primeira linha do ficheiro de output
    val scale = oct match {
      case OcNode(coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) => coords._2 / MAX_SCALE
      case _ => 1
    }
    writer.write(s"Scale ${scale}\n")
    //Vai buscar todas as shapes do worldRoot. Faz um filtro para ir buscar só as que tem o DrawMode.FILL e depois por cada uma constroi uma linha e escreve essa linha no ficheiro
    getAllShapesFromRoot(root).filter(shape => {
      shape.asInstanceOf[Shape3D].getDrawMode == DrawMode.FILL
    }).map(shape => {
      val rgb = s"(${(shape.asInstanceOf[Shape3D].getMaterial.asInstanceOf[PhongMaterial].getDiffuseColor.getRed * 255).asInstanceOf[Int]},${(shape.asInstanceOf[Shape3D].getMaterial.asInstanceOf[PhongMaterial].getDiffuseColor.getGreen * 255).asInstanceOf[Int]},${(shape.asInstanceOf[Shape3D].getMaterial.asInstanceOf[PhongMaterial].getDiffuseColor.getBlue * 255).asInstanceOf[Int]})"
      writer.write(s"${if (shape.isInstanceOf[Box]) "Cube" else "Cylinder"} ${rgb} ${shape.getTranslateX} ${shape.getTranslateY} ${shape.getTranslateZ} ${shape.getScaleX} ${shape.getScaleY} ${shape.getScaleZ}\n")
    })
    writer.close
  }
}
