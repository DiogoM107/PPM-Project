package main.scala.ppm

import javafx.application.Application
import javafx.stage.{FileChooser, Stage}
import main.scala.ppm.ImpureLayer._
import main.scala.ppm.InitSubScene._
import main.scala.ppm.PureLayer._
import main.scala.ppm.TextUI._

import java.io.File
import java.nio.file.Paths
import scala.annotation.tailrec

class Main extends Application {


  /*
    Additional information about JavaFX basic concepts (e.g. Stage, Scene) will be provided in week7
   */
  override def start(stage: Stage): Unit = {

    //Get and print program arguments (args: Array[String])
    val params = getParameters
    println("Program arguments:" + params.getRaw)

    //octree = startTextUI()

    runGUI(stage)

    stage.setTitle("PPM Project 21/22")
    stage.setScene(scene)
    stage.show

  }

  override def init(): Unit = {
    println("init")
  }

  override def stop(): Unit = {
    println("stopped")
  }

  def startTextUI(): Octree[Placement] = {
    val fileName = getFileName()
    val shapesAndScale = createShapesAndScaleFromFile(fileName)
    shapesAndScale._1.map(x => worldRoot.getChildren.add(x))
    octree = createOcTree(MAX_SCALE * shapesAndScale._2, worldRoot, shapesAndScale._1)
    octree = runWithTextUI()
    octree
  }

  //TODO - Enquanto esta funcao corre, o stage não é carregado..
  def runWithTextUI(): Octree[Placement] = {

    @tailrec
    def runLoop (): Octree[Placement] = {
      val opt = getOption()
      opt match {
        case 1 => {
          octree = scaleOctree(2, octree, worldRoot)
          writeToFile(OUTPUT_FILE, octree)
          runLoop()
        }
        case 2 => {
          octree = scaleOctree(0.5, octree, worldRoot)
          writeToFile(OUTPUT_FILE, octree)
          runLoop()
        }
        case 3 => {
          octree = mapColourEffect(sepia, octree)
          writeToFile(OUTPUT_FILE, octree)
          runLoop()
        }
        case 4 => {
          octree = mapColourEffect(greenRemove, octree)
          writeToFile(OUTPUT_FILE, octree)
          runLoop()
        }
        case 5 => {
          writeToFile(OUTPUT_FILE, octree)
          octree
        }
      }
    }
    runLoop()
  }

  def runGUI(stage: Stage) = {

    val fileChooser: FileChooser = new FileChooser()
    fileChooser.setTitle("Ficheiro de configuração.")
    val currentDir = Paths.get(".").toAbsolutePath().normalize().toString();
    fileChooser.setInitialDirectory(new File(currentDir))
    val extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt")
    fileChooser.getExtensionFilters.add(extFilter)
    val file = fileChooser.showOpenDialog(stage)
    println(s"File: ${file}")

    if (file != null) {
      val shapesAndScale = createShapesAndScaleFromFile(file.getName)
      shapesAndScale._1.map(x => worldRoot.getChildren.add(x))
      octree = createOcTree(MAX_SCALE * shapesAndScale._2, worldRoot, shapesAndScale._1)
    }
    else {
      println("Programa terminado!")
      System.exit(0)
    }
  }

}

object FxApp {

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

