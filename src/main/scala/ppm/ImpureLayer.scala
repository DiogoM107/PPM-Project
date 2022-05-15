package main.scala.ppm

import javafx.scene.Group
import javafx.scene.paint.{ Color, PhongMaterial }
import javafx.scene.shape.{ Box, Cylinder, DrawMode, Shape3D }
import main.scala.ppm.InitSubScene.{ blueMaterial, camVolume, whiteMaterial }
import main.scala.ppm.PureLayer.{ MAX_SCALE, Placement, getAllShapesFromRoot }

import java.io.{ File, PrintWriter }
import java.lang.reflect.InvocationTargetException
import scala.annotation.tailrec
import scala.io.Source

object ImpureLayer {

  val OUTPUT_FILE = "output.txt"

  /**
   * Funcao que vai criar as shapes e definir o scale inicial
   *
   * @param file - O nome/caminho do ficheiro
   * @return - a lista das shapes carregadas e o scale inicial
   */
  def createShapesAndScaleFromFile( file: String ): Option[ (List[ Shape3D ], Double) ] = {
    val lines = Source.fromFile( file ).getLines().toList
    try {
      Some( getShapesFromList( lines ) )
    }
    catch {
      case e: NumberFormatException =>
        println( "Erro ao formatar um número." )
        None
      case _: NoSuchElementException =>
        println( "Não foi encontrado o atributo esperado." )
        None
    }
  }

  /**
   * Vai converter a lista de String numa lista de Shapes
   *
   * @param lst - a lista de Strings
   * @return - a lista de shapes
   */
  //T1
  @throws( classOf[ InvocationTargetException ] )
  def getShapesFromList( lst: List[ String ] ): (List[ Shape3D ], Double) = {

    //Se, ao corrermos a aplicacao, carregarmos o ficheiro output.txt em vez do config.txt, vai haver uma linha com a ultima scale da octree
    //Esta funcao serve para ver se esse scale existe, e se existir, devolve esse scala e um true para informar que essa linha existia
    def checkScale( line: String ): (Double, Boolean) = {
      val elem = line.split( " " )
      elem( 0 ).toLowerCase() match {
        case "scale" =>
          (elem( 1 ).toDouble, true)
        case _ => (1, false)
      }
    }

    //Vai converter a lista com as linhas do ficheiro com informação de shapes numa lista de shapes
    @tailrec
    def createObjs( lst: List[ String ], objs: List[ Shape3D ] ): List[ Shape3D ] = {
      lst match {
        case Nil => objs
        case x :: tail =>
          val elem = x.split( " " )
          val rgb = elem( 1 ).replace( "(", "" ).replace( ")", "" ).split( "," )
          val color = new PhongMaterial()
          color.setDiffuseColor( Color.rgb( rgb( 0 ).toInt, rgb( 1 ).toInt, rgb( 2 ).toInt ) )
          val (transX, transY, transZ) = (elem( 2 ).toDouble, elem( 3 ).toDouble, elem( 4 ).toDouble)
          val (scaleX, scaleY, scaleZ) = (elem( 5 ).toDouble, elem( 6 ).toDouble, elem( 7 ).toDouble)
          elem( 0 ).toLowerCase() match {
            case "cylinder" =>
              val cylinder = new Cylinder( 0.5, 1, 10 )
              cylinder.setTranslateX( transX )
              cylinder.setTranslateY( transY )
              cylinder.setTranslateZ( transZ )
              cylinder.setScaleX( scaleX )
              cylinder.setScaleY( scaleY )
              cylinder.setScaleZ( scaleZ )
              cylinder.setMaterial( color )
              cylinder.setDrawMode( DrawMode.FILL )
              createObjs( tail, cylinder :: objs )
            case "cube" =>
              val box = new Box( 1, 1, 1 )
              box.setTranslateX( transX )
              box.setTranslateY( transY )
              box.setTranslateZ( transZ )
              box.setScaleX( scaleX )
              box.setScaleY( scaleY )
              box.setScaleZ( scaleZ )
              box.setMaterial( color )
              box.setDrawMode( DrawMode.FILL )
              createObjs( tail, box :: objs )
            case _ =>
              createObjs( tail, objs )
          }
      }
    }

    val (scale, existsScale) = checkScale( lst.head )

    if ( existsScale ) {
      (createObjs( lst.tail, Nil ), scale)
    }
    else {
      (createObjs( lst, Nil ), scale)
    }

  }

  /**
   * Funcao usada para criar uma particao
   *
   * @param placement - o placement
   * @return - a particao
   */
  // Cria um cubo com o placement enviado como parametro
  def createShapeCube( placement: Placement ): Box = {
    val ((x, y, z), size) = placement

    val box = new Box( size, size, size )
    box.setTranslateX( size / 2 + x )
    box.setTranslateY( size / 2 + y )
    box.setTranslateZ( size / 2 + z )

    // Aqui temos que ver se a particao está dentro do campo de visão do CamVolume, para definirmos a cor
    if ( box.asInstanceOf[ Shape3D ].getBoundsInParent.intersects( camVolume.asInstanceOf[ Shape3D ].getBoundsInParent ) ) {
      box.setMaterial( blueMaterial )
    }
    else {
      box.setMaterial( whiteMaterial )
    }
    box.setDrawMode( DrawMode.LINE )
    box
  }

  /**
   * Funcao usada para escrever no ficheiro de output
   *
   * @param file - o nome do ficheiro de output
   * @param root - o worldRoot
   * @param oct  - a octree
   */
  def writeToFile( file: String, oct: Octree[ Placement ], root: Group ): Unit = {
    val writer = new PrintWriter( new File( file ) )
    //Aqui vai buscar o valor do scale da octree final para escrever na primeira linha do ficheiro de output
    val scale = oct match {
      case OcNode( coords, _, _, _, _, _, _, _, _ ) => coords._2 / MAX_SCALE
      case _ => 1
    }
    writer.write( s"Scale $scale\n" )
    //Vai buscar todas as shapes do worldRoot. Faz um filtro para ir buscar só as que tem o DrawMode.FILL e depois por cada uma constroi uma linha e escreve essa linha no ficheiro
    getAllShapesFromRoot( root ).filter( shape => {
      shape.getDrawMode == DrawMode.FILL
    } ).map( shape => {
      val rgb = s"(${( shape.getMaterial.asInstanceOf[ PhongMaterial ].getDiffuseColor.getRed * 255 ).asInstanceOf[ Int ]},${( shape.getMaterial.asInstanceOf[ PhongMaterial ].getDiffuseColor.getGreen * 255 ).asInstanceOf[ Int ]},${( shape.getMaterial.asInstanceOf[ PhongMaterial ].getDiffuseColor.getBlue * 255 ).asInstanceOf[ Int ]})"
      writer.write( s"${if ( shape.isInstanceOf[ Box ] ) "Cube" else "Cylinder"} $rgb ${shape.getTranslateX} ${shape.getTranslateY} ${shape.getTranslateZ} ${shape.getScaleX} ${shape.getScaleY} ${shape.getScaleZ}\n" )
    } )
    writer.close()
  }

  @tailrec
  def drawShapesInRoot( shapes: List[ Shape3D ], root: Group ): Boolean = {
    shapes match {
      case Nil => true
      case x :: xs =>
        root.getChildren.add( x )
        drawShapesInRoot( xs, root )
    }
  }

  def removeShapesFromRoot( shapes: List[ Shape3D ], root: Group ): Boolean = {
    shapes.map( x => root.getChildren.remove( x ) )
    true
  }

  //T5
  def mapColourEffect( func: Color => Color, oct: Octree[ Placement ] ): Octree[ Placement ] = {
    oct match {
      case OcEmpty => OcEmpty
      case OcLeaf( (placement: Placement, shapes: List[ Shape3D ]) ) =>
        val list = shapes
        list.map( x => {
          val y = x
          val c = x.getMaterial.asInstanceOf[ PhongMaterial ].getDiffuseColor
          val pm = new PhongMaterial()
          pm.setDiffuseColor( func( c ) )
          y.setMaterial( pm )
          y
        } )
        OcLeaf( (placement, list) )
      case OcNode( coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11 ) =>
        OcNode( coords, mapColourEffect( func, up_00 ), mapColourEffect( func, up_01 ), mapColourEffect( func, up_10 ), mapColourEffect( func, up_11 ),
          mapColourEffect( func, down_00 ), mapColourEffect( func, down_01 ), mapColourEffect( func, down_10 ), mapColourEffect( func, down_11 ) )
    }
  }
}
