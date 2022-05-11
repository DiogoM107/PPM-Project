package main.scala.ppm

import javafx.scene.Node
import javafx.scene.shape.{Box, DrawMode, Shape3D}
import main.scala.ppm.InitSubScene.worldRoot
import main.scala.ppm.PureLayer.{Placement, Section, getAllShapesFromRoot}

import java.io.{File, PrintWriter}

object ImpureLayer {

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

  def writeToFile(file: String, oct: Octree[Placement]) = {
    val writer = new PrintWriter(new File(file))
    //Deve ir buscar apenas as shapes dentro da octree
    getAllShapesFromRoot(worldRoot).filter(shape => {
      shape.asInstanceOf[Shape3D].getDrawMode == DrawMode.FILL
    }).map(shape => {
      writer.write(s"Class: ${if(shape.isInstanceOf[Box]) "Box" else "Cylinder"}, ${shape.getTranslateX}, ${shape.getTranslateY}, ${shape.getTranslateZ}, ${shape.getScaleX}, ${shape.getScaleY}, ${shape.getScaleZ}\n")
    })
    writer.close
  }
}
