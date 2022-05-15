package main.scala.ppm

import javafx.geometry.Bounds
import javafx.scene.{ Group, Node }
import javafx.scene.paint.{ Color, PhongMaterial }
import javafx.scene.shape.{ Box, Cylinder, DrawMode, Shape3D }
import main.scala.ppm.ImpureLayer.{ createShapeCube, drawShapesInRoot, removeShapesFromRoot }
import main.scala.ppm.InitSubScene.{ blueMaterial, camVolume, whiteMaterial, worldRoot }

import scala.annotation.tailrec
import scala.io.Source
import scala.jdk.CollectionConverters.CollectionHasAsScala

object PureLayer {

  val MAX_SCALE = 32.0
  val MIN_SCALE = 4.0

  //Auxiliary types
  type Point = (Double, Double, Double)
  type Size = Double
  type Placement = (Point, Size) //1st point: origin, 2nd point: size

  //Shape3D is an abstract class that extends javafx.scene.Node
  //Box and Cylinder are subclasses of Shape3D
  type Section = (Placement, List[ Node ]) //example: ( ((0.0,0.0,0.0), 2.0), List(new Cylinder(0.5, 1, 10)))

  var octree: Octree[ Placement ] = _

  //T2

  /**
   * Aqui vamos começar a criar a octree
   *
   * @param scale      - o tamanho do maior nó da octree. Segundo o enunciado, deve ter um tamanho de 32
   * @param root       - o worldRoot, precisamos como argumento para poder inserir as partições (Nodes) criados no mapa
   * @param shapesList - Lista com as shapes que carregamos pelo ficheiro. Precisamos para saber que partições é que temos que desenhar
   * @return
   */
  def createOcTree( scale: Double, root: Group, shapesList: List[ Shape3D ], depth: Int ): Octree[ Placement ] = {
    val placement: Placement = ((0, 0, 0), scale)
    createOcNode( placement, root, shapesList, depth )
  }

  /**
   * Aqui criamos, recursivamente, todos as partições da OcTree.
   *
   * @param placement  - O placement da partição superior
   * @param root       - O worldRoot, necessário para desenhar as partições
   * @param shapesList - A lista de shapes que carregamos no ficheiro
   * @return
   */
  def createOcNode( placement: Placement, root: Group, shapesList: List[ Shape3D ], depth: Int ): Octree[ Placement ] = {
    val ((x, y, z), size) = placement
    val newSize = size / 2

    //Primeiro passo é criar a partição
    val nodeBox = createShapeCube( placement )
    if ( checkConditionWithBounds( nodeBox, shapesList, containsAShapeFunc ) ) {
      //Se a particao que criamos tiver alguma shape 100% dentro dela, então vamos desenhá-la
      drawShapesInRoot( List( nodeBox ), root )
      //O tamanho minimo duma partição foi definido como 2, portanto será aí que vai parar a recursividade
      if ( size > MIN_SCALE && depth > 0 ) {
        OcNode( placement,
          createOcNode( ((x, y + newSize, z), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x + newSize, y + newSize, z), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x, y + newSize, z + newSize), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x + newSize, y + newSize, z + newSize), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x, y, z), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x + newSize, y, z), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x, y, z + newSize), newSize), root, shapesList, depth - 1 ),
          createOcNode( ((x + newSize, y, z + newSize), newSize), root, shapesList, depth - 1 )
        )
      }
      else {
        //Se estivermos no tamanho minimo, então vamos criar uma OcLeaf, que é uma partição que tem uma lista com todos os objetos contidos nela
        val newSection: Section = (placement, getShapesWithCondition( nodeBox, shapesList, containsAShapeFunc ))
        OcLeaf( newSection )
      }
    }
    //Pode acontecer uma partição ter um objeto que não está 100% contido nela. Nesse caso, vamos tornar o OcNode pai numa ocleaf,
    // para ser possível ter uma ocleaf com o objeto 100% dentro dele
    else if ( checkConditionWithBounds( nodeBox, shapesList, intersectsAShapeWithoutContainingFunc ) ) {
      val newSection: Section = (placement, getShapesWithCondition( nodeBox, shapesList, intersectsAShapeFunc ))
      OcLeaf( newSection )
    }
    //Se a partição não tiver nenhum objeto contido nela nem está a intersectar nada, então não vale a pena desenvolvê-la.
    else OcEmpty
  }

  def getShapesWithCondition( node: Node, shapesList: List[ Shape3D ], func: (Bounds, Bounds) => Boolean ): List[ Shape3D ] = {
    def checkCondition( node: Node, list: List[ Shape3D ], contained: List[ Shape3D ] ): List[ Shape3D ] = {
      list match {
        case Nil => contained
        case x :: xs =>
          if ( func( node.getBoundsInParent, x.getBoundsInParent ) ) {
            checkCondition( node, xs, x :: contained )
          }
          else {
            checkCondition( node, xs, contained )
          }

      }
    }

    checkCondition( node, shapesList, Nil )
  }

  def checkConditionWithBounds( node: Node, shapesList: List[ Shape3D ], func: (Bounds, Bounds) => Boolean ): Boolean = {
    def checkCondition( node: Node, list: List[ Shape3D ] ): Boolean = {
      list match {
        case Nil => false
        case x :: xs =>
          if ( func( node.getBoundsInParent, x.getBoundsInParent ) ) {
            true
          }
          else {
            checkCondition( node, xs )
          }
      }
    }

    checkCondition( node, shapesList )
  }

  def intersectsAShapeFunc( bounds: Bounds, secondBounds: Bounds ): Boolean = {
    bounds.intersects( secondBounds )
  }

  def intersectsAShapeWithoutContainingFunc( bounds: Bounds, secondBounds: Bounds ): Boolean = {
    bounds.intersects( secondBounds ) && !bounds.contains( secondBounds )
  }

  def containsAShapeFunc( bounds: Bounds, secondBounds: Bounds ): Boolean = {
    bounds.contains( secondBounds )
  }

  def containsAShapeWithoutIntersectingFunc( bounds: Bounds, secondBounds: Bounds ): Boolean = {
    bounds.contains( secondBounds ) && !intersectsAShapeWithoutContainingFunc( bounds, secondBounds )
  }

  /**
   * Vai converter a lista de String numa lista de Shapes
   *
   * @param lst - a lista de Strings
   * @return - a lista de shapes
   */
  //T1
  def getShapesFromList( lst: List[ String ] ): (List[ Shape3D ], Double) = {

    //Se, ao corrermos a aplicacao, carregarmos o ficheiro output.txt em vez do config.txt, vai haver uma linha com a ultima scale da octree
    //Esta funcao serve para ver se esse scale existe, e se existir, devolve esse scala e um true para informar que essa linha existia
    def checkScale( line: String ): (Double, Boolean) = {
      val elem = line.split( " " )
      elem( 0 ).toLowerCase() match {
        case "scale" => {
          (elem( 1 ).toDouble, true)
        }
        case _ => (1, false)
      }
    }

    //Vai converter a lista com as linhas do ficheiro com informação de shapes numa lista de shapes
    @tailrec
    def createObjs( lst: List[ String ], objs: List[ Shape3D ] ): List[ Shape3D ] = {
      lst match {
        case Nil => objs
        case x :: tail => {
          val elem = x.split( " " )
          val rgb = elem( 1 ).replace( "(", "" ).replace( ")", "" ).split( "," )
          val color = new PhongMaterial()
          color.setDiffuseColor( Color.rgb( rgb( 0 ).toInt, rgb( 1 ).toInt, rgb( 2 ).toInt ) )
          val (transX, transY, transZ) = (elem( 2 ).toDouble, elem( 3 ).toDouble, elem( 4 ).toDouble)
          val (scaleX, scaleY, scaleZ) = (elem( 5 ).toDouble, elem( 6 ).toDouble, elem( 7 ).toDouble)
          elem( 0 ).toLowerCase() match {
            case "cylinder" => {
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
            }
            case "cube" => {
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

            }
            case _ => {
              createObjs( tail, objs )
            }
          }
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
   * Funcao devolve todas as shapes que existem no worldRoot, ignorando o camVolume
   *
   * @param root - worldRoot
   * @return - a lista de shapes
   */
  def getAllShapesFromRoot( root: Group ): List[ Shape3D ] = {
    def aux( children: List[ Node ], shapes: List[ Shape3D ] ): List[ Shape3D ] = {
      children match {
        case Nil => shapes
        case x :: xs =>
          if ( x.isInstanceOf[ Shape3D ] && x != camVolume ) {
            aux( xs, x.asInstanceOf[ Shape3D ] :: shapes )
          }
          else {
            aux( xs, shapes )
          }
      }
    }

    val list = root.getChildren.asScala.toList
    aux( list, Nil )
  }

  /**
   * Funcao vai percorrer todas as shapes enviadas na lista e verificar se estão a intersectar o camVolume
   * Se não estiverem, devem ficar em branco
   *
   * @param shapes - a lista das shapes
   * @return - a lista das shapes com as cores alteradas
   */
  //T3
  //TODO - Esta função vai alterar diretamente as shapes do worldRoot
  def getShapesWithChangedColor( shapes: List[ Shape3D ] ): List[ Shape3D ] = {
    shapes match {
      case Nil => Nil
      case x :: xs =>
        if ( x.getDrawMode != DrawMode.FILL ) {
          if ( !x.getBoundsInParent.intersects( camVolume.getBoundsInParent ) ) {
            x.setMaterial( whiteMaterial )
          }
          else {
            x.setMaterial( blueMaterial )
          }
        }
        x :: getShapesWithChangedColor( xs )
    }
  }

  //T1

  /**
   * Funcao que vai criar as shapes e definir o scale inicial
   *
   * @param file - O nome/caminho do ficheiro
   * @return - a lista das shapes carregadas e o scale inicial
   */
  def createShapesAndScaleFromFile( file: String ): (List[ Shape3D ], Double) = {
    val lines = Source.fromFile( file ).getLines().toList
    getShapesFromList( lines )
  }

  //T4

  /**
   * Funcao vai receber uma octree e um factor de scale, e devolve uma nova octree com esse valor de scale
   *
   * @param fact - o factor de scale
   * @param oct  - a octree
   * @param root - o worldRoot
   * @return - a octree escalada
   */
  def scaleOctree( fact: Double, oct: Octree[ Placement ], root: Group ): Octree[ Placement ] = {
    //TODO - deve devolver a lista de objetos nova e nao alterar diretamente, para isso teriamos que retirar os objetos do worldRoot e voltar a colocar
    def scaleShapes( shapes: List[ Shape3D ] ): List[ Shape3D ] = {
      shapes match {
        case Nil => Nil
        case x :: tail =>
          val newShape = x
          newShape.setTranslateX( x.getTranslateX * fact )
          newShape.setTranslateY( x.getTranslateY * fact )
          newShape.setTranslateZ( x.getTranslateZ * fact )
          newShape.setScaleX( x.getScaleX * fact )
          newShape.setScaleY( x.getScaleY * fact )
          newShape.setScaleZ( x.getScaleZ * fact )
          newShape :: scaleShapes( tail )
      }
    }

    def scaleOctNodes[ A ]( oct: Octree[ Placement ] ): Octree[ Placement ] = {
      oct match {
        case OcEmpty => OcEmpty
        case OcLeaf( section ) => {
          val (((x, y, z), size), shapes) = section.asInstanceOf[ Section ]
          val newSection: Section = (((x * fact, y * fact, z * fact), size * fact), shapes)
          OcLeaf( newSection )
        }
        case OcNode( ((x, y, z), size), up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11 ) => {
          val newCoords = ((x * fact, y * fact, z * fact), size * fact)
          OcNode( newCoords, scaleOctNodes( up_00 ), scaleOctNodes( up_01 ), scaleOctNodes( up_10 ), scaleOctNodes( up_11 ), scaleOctNodes( down_00 ), scaleOctNodes( down_01 ), scaleOctNodes( down_10 ), scaleOctNodes( down_11 ) )
        }
      }
    }

    val shapes = getAllShapesFromRoot( root )
    val newShapes = getShapesWithChangedColor( scaleShapes( shapes ) )
    removeShapesFromRoot( shapes, root )
    drawShapesInRoot( newShapes, root )
    scaleOctNodes( oct )
  }

  def sepia( color: Color ): Color = {
    val r = Math.min( ( ( ( color.getRed * 0.4 * 255 ) ) + ( ( color.getGreen * 0.77 * 255 ) ) + ( ( color.getBlue * 0.2 * 255 ) ) ).asInstanceOf[ Int ], 255 )
    val g = Math.min( ( ( ( color.getRed * 0.35 * 255 ) ) + ( ( color.getGreen * 0.69 * 255 ) ) + ( ( color.getBlue * 0.17 * 255 ) ) ).asInstanceOf[ Int ], 255 )
    val b = Math.min( ( ( ( color.getRed * 0.27 * 255 ) ) + ( ( color.getGreen * 0.53 * 255 ) ) + ( ( color.getBlue * 0.13 * 255 ) ) ).asInstanceOf[ Int ], 255 )
    Color.rgb( r, g, b )
  }

  def greenRemove( color: Color ): Color = {
    Color.rgb( ( color.getRed * 255 ).asInstanceOf[ Int ], 0, ( color.getBlue * 255 ).asInstanceOf[ Int ] )
  }

  //TODO - completar
  /*
  def getAllShapesAndPartitionsFromOctree(octree: Octree[Placement]): List[Shape3D] = {
    octree match {
      case OcEmpty => Nil
      case OcLeaf((placement: Placement, shapes: List[Shape3D])) => shapes :+ createShapeCube(placement)
      case OcNode(_, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) =>
        getAllShapesAndPartitionsFromOctree(up_00) :: getAllShapesAndPartitionsFromOctree(up_01) :: getAllShapesAndPartitionsFromOctree(up_10) :: getAllShapesAndPartitionsFromOctree(up_11) :: getAllShapesAndPartitionsFromOctree(down_00) :: getAllShapesAndPartitionsFromOctree(down_01) :: getAllShapesAndPartitionsFromOctree(down_10) :: getAllShapesAndPartitionsFromOctree(down_11)
    }
  }

   */

}
