package main.scala.ppm

import javafx.application.Application
import javafx.scene.Group
import javafx.stage.{ FileChooser, Stage }
import main.scala.ppm.ImpureLayer._
import main.scala.ppm.InitSubScene._
import main.scala.ppm.PureLayer._
import main.scala.ppm.TextUI._

import java.io.File
import java.nio.file.Paths
import scala.annotation.tailrec

class Main extends Application {

  override def start( stage: Stage ): Unit = {

    //Get and print program arguments (args: Array[String])
    val params = getParameters
    println( "Program arguments:" + params.getRaw )

    //Escolher qual a interface que deve correr:
    //octree = startTextUI( worldRoot )
    octree = startGUI( stage, worldRoot )
    if ( octree == OcEmpty ) {
      println( "Programa terminado." )
      System.exit( 0 )
    }

    stage.setTitle( "PPM Project 21/22" )
    stage.setScene( scene )
    stage.show()

  }

  override def init( ): Unit = {
    println( "init" )
  }

  override def stop( ): Unit = {
    println( "stopped" )
  }

  def startTextUI( root: Group ): Octree[ Placement ] = {
    val fileName = getFileName
    val depth = getDepth
    createShapesAndScaleFromFile( fileName ) match {
      case Some( (shapes, scale) ) =>
        drawShapesInRoot( shapes, root )
        val oct = createOcTree( MAX_SCALE * scale, root, shapes, depth )
        runWithTextUI( oct, root )
      case None =>
        println( "Erro na leitura do ficheiro." )
        OcEmpty
    }

  }

  def runWithTextUI( octree: Octree[ Placement ], root: Group ): Octree[ Placement ] = {

    //TODO - encontrar melhor forma de fazer isto
    var oct: Octree[ Placement ] = octree

    @tailrec
    def runLoop( ): Octree[ Placement ] = {
      val opt = getOption
      opt match {
        case 1 =>
          oct = scaleOctree( 2, oct, root )
          writeToFile( OUTPUT_FILE, oct, root )
          runLoop()
        case 2 =>
          oct = scaleOctree( 0.5, oct, root )
          writeToFile( OUTPUT_FILE, oct, root )
          runLoop()
        case 3 =>
          oct = mapColourEffect( sepia, oct )
          writeToFile( OUTPUT_FILE, oct, root )
          runLoop()
        case 4 =>
          oct = mapColourEffect( greenRemove, oct )
          writeToFile( OUTPUT_FILE, oct, root )
          runLoop()
        case 5 =>
          writeToFile( OUTPUT_FILE, oct, root )
          oct
      }
    }

    runLoop()
  }

  def startGUI( stage: Stage, root: Group ): Octree[ Placement ] = {

    //TODO - tem que pedir input ao user
    val depth = 3

    runGUI( stage, depth, root )
  }

  def runGUI( stage: Stage, depth: Int, root: Group ): Octree[ Placement ] = {

    val fileChooser: FileChooser = new FileChooser()
    fileChooser.setTitle( "Ficheiro de configura????o." )
    val currentDir = Paths.get( "." ).toAbsolutePath.normalize().toString
    fileChooser.setInitialDirectory( new File( currentDir ) )
    val extFilter = new FileChooser.ExtensionFilter( "TXT files (*.txt)", "*.txt" )
    fileChooser.getExtensionFilters.add( extFilter )
    val file = fileChooser.showOpenDialog( stage )
    println( s"File: $file" )

    if ( file != null ) {
      createShapesAndScaleFromFile( file.getName ) match {
        case Some( (shapes, scale) ) =>
          drawShapesInRoot( shapes, root )
          createOcTree( MAX_SCALE * scale, root, shapes, depth )
        case None =>
          println( "Erro na leitura do ficheiro." )
          OcEmpty
      }

    }
    else {
      OcEmpty
    }
  }

}

object FxApp {

  def main( args: Array[ String ] ): Unit = {
    Application.launch( classOf[ Main ], args: _* )
  }
}