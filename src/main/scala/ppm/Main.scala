package main.scala.ppm

import javafx.application.Application
import javafx.scene.input.KeyCode
import javafx.scene.shape.Shape3D
import javafx.stage.Stage
import main.scala.ppm.ImpureLayer._
import main.scala.ppm.InitSubScene._
import main.scala.ppm.PureLayer._
import main.scala.ppm.TextUI._

class Main extends Application {

  val OUTPUT_FILE = "output.txt"

  /*
    Additional information about JavaFX basic concepts (e.g. Stage, Scene) will be provided in week7
   */
  override def start(stage: Stage): Unit = {

    //Get and print program arguments (args: Array[String])
    val params = getParameters
    println("Program arguments:" + params.getRaw)

    val fileName = getFileName()

    //setup and start the Stage
    stage.setTitle("PPM Project 21/22")
    stage.setScene(scene)
    stage.show

    val shapes = createShapesFromFile(fileName, worldRoot)
    shapes.map(x => worldRoot.getChildren.add(x))

    //octree = runWithTextUI(fileName, octree)

    runTest(fileName, shapes)

  }

  override def init(): Unit = {
    println("init")
  }

  override def stop(): Unit = {
    println("stopped")
  }

  def runWithTextUI(fileName: String, octree: Octree[Placement]): Octree[Placement] = {

    InitSubScene
    val opt = getOption()
    opt match {
      case 1 => {
        scaleOctree(2, octree, worldRoot)
      }
      case 2 => {
        scaleOctree(0.5, octree, worldRoot)
      }
      case 3 => {
        mapColourEffect(sepia, octree)
      }
      case 4 => {
        mapColourEffect(greenRemove, octree)
      }
    }
  }

  def runTest(fileName: String, shapes: List[Shape3D]) = {

    var octree = createOcTree(MAX_SCALE, worldRoot, shapes)
    //Mouse left click interaction
    scene.setOnMouseClicked((event) => {
      camVolume.setTranslateX(camVolume.getTranslateX + 2)
      //TODO - isto devia devolver a lista das shapes com as cores alteradas, e voltar a ser criada a octree
      changeColor(getAllShapesFromRoot(worldRoot))
      writeToFile(OUTPUT_FILE, octree)
      //worldRoot.getChildren.removeAll()
    })

    //TODO - apenas para testar o scaleOctree
    scene.setOnKeyPressed(k => {
      if (k.getCode == KeyCode.UP)
        octree = scaleOctree(2, octree, worldRoot)
      else if (k.getCode() == KeyCode.DOWN)
        octree = scaleOctree(0.5, octree, worldRoot)
      else if (k.getCode() == KeyCode.S)
        octree = mapColourEffect(sepia, octree)
      else if (k.getCode() == KeyCode.E)
        octree = mapColourEffect(greenRemove, octree)
    })
  }

}

object FxApp {

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

