package main.scala.ppm

import javafx.application.Application
import javafx.geometry.{Insets, Pos}
import javafx.scene._
import javafx.scene.input.KeyCode
import javafx.scene.layout.StackPane
import javafx.scene.paint.{Color, PhongMaterial}
import javafx.scene.shape._
import javafx.scene.transform.Rotate
import javafx.stage.Stage

import java.io.{File, PrintWriter}
import scala.annotation.tailrec
import scala.io.Source
import scala.jdk.CollectionConverters.CollectionHasAsScala


class Main extends Application {

  val MAX_SCALE = 32.0
  val MIN_SCALE = 4.0

  //Auxiliary types
  type Point = (Double, Double, Double)
  type Size = Double
  type Placement = (Point, Size) //1st point: origin, 2nd point: size

  //Shape3D is an abstract class that extends javafx.scene.Node
  //Box and Cylinder are subclasses of Shape3D
  type Section = (Placement, List[Node]) //example: ( ((0.0,0.0,0.0), 2.0), List(new Cylinder(0.5, 1, 10)))

  /*
    Additional information about JavaFX basic concepts (e.g. Stage, Scene) will be provided in week7
   */
  override def start(stage: Stage): Unit = {

    //Get and print program arguments (args: Array[String])
    val params = getParameters
    println("Program arguments:" + params.getRaw)

    //Materials to be applied to the 3D objects
    val redMaterial = new PhongMaterial()
    redMaterial.setDiffuseColor(Color.rgb(150, 0, 0))

    val greenMaterial = new PhongMaterial()
    greenMaterial.setDiffuseColor(Color.rgb(0, 255, 0))

    val blueMaterial = new PhongMaterial()
    blueMaterial.setDiffuseColor(Color.rgb(0, 0, 150))

    val blackMaterial = new PhongMaterial()
    blackMaterial.setDiffuseColor(Color.rgb(0, 0, 0))

    val whiteMaterial = new PhongMaterial()
    whiteMaterial.setDiffuseColor(Color.rgb(255, 255, 255))

    //3D objects
    val lineX = new Line(0, 0, 200, 0)
    lineX.setStroke(Color.GREEN)

    val lineY = new Line(0, 0, 0, 200)
    lineY.setStroke(Color.YELLOW)

    val lineZ = new Line(0, 0, 200, 0)
    lineZ.setStroke(Color.LIGHTSALMON)
    lineZ.getTransforms().add(new Rotate(-90, 0, 0, 0, Rotate.Y_AXIS))

    val camVolume = new Cylinder(10, 50, 10)
    camVolume.setTranslateX(1)
    camVolume.getTransforms().add(new Rotate(45, 0, 0, 0, Rotate.X_AXIS))
    camVolume.setMaterial(blueMaterial)
    camVolume.setDrawMode(DrawMode.LINE)

    val wiredBox = new Box(32, 32, 32)
    wiredBox.setTranslateX(16)
    wiredBox.setTranslateY(16)
    wiredBox.setTranslateZ(16)
    wiredBox.setMaterial(redMaterial)
    wiredBox.setDrawMode(DrawMode.LINE)

    val cylinder1 = new Cylinder(0.5, 1, 10)
    cylinder1.setTranslateX(2)
    cylinder1.setTranslateY(2)
    cylinder1.setTranslateZ(2)
    cylinder1.setScaleX(2)
    cylinder1.setScaleY(2)
    cylinder1.setScaleZ(2)
    cylinder1.setMaterial(greenMaterial)

    val box1 = new Box(1, 1, 1) //
    box1.setTranslateX(5)
    box1.setTranslateY(5)
    box1.setTranslateZ(5)
    box1.setMaterial(greenMaterial)

    // 3D objects (group of nodes - javafx.scene.Node) that will be provide to the subScene
    val worldRoot: Group = new Group(wiredBox, camVolume, lineX, lineY, lineZ)

    // Camera
    val camera = new PerspectiveCamera(true)

    val cameraTransform = new CameraTransformer
    cameraTransform.setTranslate(0, 0, 0)
    cameraTransform.getChildren.add(camera)
    camera.setNearClip(0.1)
    camera.setFarClip(10000.0)

    camera.setTranslateZ(-500)
    camera.setFieldOfView(20)
    cameraTransform.ry.setAngle(-45.0)
    cameraTransform.rx.setAngle(-45.0)
    worldRoot.getChildren.add(cameraTransform)

    // SubScene - composed by the nodes present in the worldRoot
    val subScene = new SubScene(worldRoot, 800, 600, true, SceneAntialiasing.BALANCED)
    subScene.setFill(Color.DARKSLATEGRAY)
    subScene.setCamera(camera)

    // main.scala.ppm.CameraView - an additional perspective of the environment
    val cameraView = new CameraView(subScene)
    cameraView.setFirstPersonNavigationEabled(true)
    cameraView.setFitWidth(350)
    cameraView.setFitHeight(225)
    cameraView.getRx.setAngle(-45)
    cameraView.getT.setZ(-100)
    cameraView.getT.setY(-500)
    cameraView.getCamera.setTranslateZ(-50)
    cameraView.startViewing

    // Position of the main.scala.ppm.CameraView: Right-bottom corner
    StackPane.setAlignment(cameraView, Pos.BOTTOM_RIGHT)
    StackPane.setMargin(cameraView, new Insets(5))

    // Scene - defines what is rendered (in this case the subScene and the cameraView)
    val root = new StackPane(subScene, cameraView)
    subScene.widthProperty.bind(root.widthProperty)
    subScene.heightProperty.bind(root.heightProperty)

    val scene = new Scene(root, 810, 610, true, SceneAntialiasing.BALANCED)

    //T3 - Estou a usar o x.asInstanceOf[Shape3D].getDrawMode != DrawMode.FILL para não mudar a cor das shapes enviadas
    def changeColor(shapes: List[Shape3D]): Unit = {
      shapes match {
        case Nil => Nil
        case x :: xs => {
          if (x.asInstanceOf[Shape3D].getDrawMode != DrawMode.FILL) {
            if (!x.asInstanceOf[Shape3D].getBoundsInParent.intersects(camVolume.getBoundsInParent)) {
              x.asInstanceOf[Shape3D].setMaterial(whiteMaterial)
            }
            else {
              x.asInstanceOf[Shape3D].setMaterial(blueMaterial)
            }
          }
          changeColor(xs)
        }
      }
    }


    //setup and start the Stage
    stage.setTitle("PPM Project 21/22")
    stage.setScene(scene)
    stage.show


    //T1
    def getShapesFromList(lst: List[String]): List[Shape3D] = {
      lst match {
        case Nil => Nil
        case x :: tail => {
          val elem = x.split(" ")
          val rgb = elem(1).replace("(", "").replace(")", "").split(",")
          val color = new PhongMaterial()
          color.setDiffuseColor(Color.rgb(rgb(0).toInt, rgb(1).toInt, rgb(2).toInt))
          val transXYZ = (elem(2).toDouble, elem(3).toDouble, elem(4).toDouble)
          val scaleXYZ = (elem(5).toDouble, elem(6).toDouble, elem(7).toDouble)
          elem(0).toLowerCase() match {
            case "cylinder" => {
              val cylinder = new Cylinder(0.5, 1, 10)
              cylinder.setTranslateX(transXYZ._1)
              cylinder.setTranslateY(transXYZ._2)
              cylinder.setTranslateZ(transXYZ._3)
              cylinder.setScaleX(scaleXYZ._1)
              cylinder.setScaleY(scaleXYZ._2)
              cylinder.setScaleZ(scaleXYZ._3)
              cylinder.setMaterial(color)
              cylinder.setDrawMode(DrawMode.FILL)
              worldRoot.getChildren.add(cylinder)
              cylinder :: getShapesFromList(tail)
            }
            case "cube" => {
              val box = new Box(1, 1, 1)
              box.setTranslateX(transXYZ._1)
              box.setTranslateY(transXYZ._2)
              box.setTranslateZ(transXYZ._3)
              box.setScaleX(scaleXYZ._1)
              box.setScaleY(scaleXYZ._2)
              box.setScaleZ(scaleXYZ._3)
              box.setMaterial(color)
              box.setDrawMode(DrawMode.FILL)
              worldRoot.getChildren.add(box)
              box :: getShapesFromList(tail)

            }
            case _ => {
              getShapesFromList(tail)
            }
          }
        }
      }

    }

    //T1
    def createShapesFromFile(file: String): List[Shape3D] = {
      val lines = Source.fromFile(file).getLines().toList
      val shapes: List[Shape3D] = getShapesFromList(lines)
      shapes
    }

    def getAllShapes(): List[Shape3D] = {
      def aux(children: List[Node], shapes: List[Shape3D]): List[Shape3D] = {
        children match {
          case Nil => shapes
          case x :: xs => {
            if (x.isInstanceOf[Shape3D] && x != camVolume) {
              aux(xs, x.asInstanceOf[Shape3D] :: shapes)
            }
            else {
              aux(xs, shapes)
            }
          }
        }
      }

      val list = worldRoot.getChildren.asScala.toList
      aux(list, Nil)
    }

    //TODO - usar nome do ficheiro que será enviado pelo terminal
    createShapesFromFile("config.txt")

    // Cria um cubo com o placement enviado como parametro
    def createShapeCube(placement: Placement): Box = {
      val xyz = placement._1
      val size = placement._2
      val box = new Box(size, size, size)
      box.setTranslateX(size / 2 + xyz._1)
      box.setTranslateY(size / 2 + xyz._2)
      box.setTranslateZ(size / 2 + xyz._3)
      box.setMaterial(blueMaterial)
      box.setDrawMode(DrawMode.LINE)
      box
    }

    def containsAShape(node: Node): Boolean = {
      def aux(node: Node, list: List[Shape3D]): Boolean = {
        list match {
          case Nil => false
          case x :: xs => {
            if (node.getBoundsInParent.contains(x.asInstanceOf[Shape3D].getBoundsInParent)) {
              true
            }
            else {
              aux(node, xs)
            }
          }
        }
      }

      val shapesList = getAllShapes()
      aux(node, shapesList)
    }

    def getContainedShapes(node: Node): List[Shape3D] = {
      def aux(node: Node, list: List[Shape3D], contained: List[Shape3D]): List[Shape3D] = {
        list match {
          case Nil => contained
          case x :: xs => {
            if (node.getBoundsInParent.contains(x.asInstanceOf[Shape3D].getBoundsInParent)) {
              aux(node, xs, x :: contained)
            }
            else {
              aux(node, xs, contained)
            }
          }
        }
      }

      val shapesList = createShapesFromFile("config.txt")
      aux(node, shapesList, Nil)
    }

    //T2 - criar uma OcTree (Falta saber como definir o MIN_SCALE
    def createOcTree(): Octree[Placement] = {
      val placement: Placement = ((0, 0, 0), MAX_SCALE)
      createOcNode(placement)
    }

    def createOcNode(placement: Placement): Octree[Placement] = {
      val xyz = placement._1
      val size = placement._2 / 2
      val nodeBox = createShapeCube(placement)
      if (containsAShape(nodeBox)) {

        worldRoot.getChildren.add(nodeBox)
        if (placement._2 > MIN_SCALE) {
          OcNode(placement,
            createOcNode((xyz._1, xyz._2 + size, xyz._3), (size)),
            createOcNode((xyz._1 + size, xyz._2 + size, xyz._3), (size)),
            createOcNode((xyz._1, xyz._2 + size, xyz._3 + size), (size)),
            createOcNode((xyz._1 + size, xyz._2 + size, xyz._3 + size), (size)),
            createOcNode(xyz, (size)),
            createOcNode((xyz._1 + size, xyz._2, xyz._3), (size)),
            createOcNode((xyz._1, xyz._2, xyz._3 + size), (size)),
            createOcNode((xyz._1 + size, xyz._2, xyz._3 + size), (size))
          )
        }
        else {
          OcLeaf(placement, getContainedShapes(nodeBox))
        }

      }
      else OcEmpty
    }

    var octree = createOcTree()

    //T4 - deve devolver a ocTree
    def scaleOctree(fact: Double, oct: Octree[Placement]) = {
      def scaleShapes(shapes: List[Shape3D]): Any = {
        shapes match {
          case Nil => Nil
          case x :: tail => {
            x.setTranslateX(x.getTranslateX * fact)
            x.setTranslateY(x.getTranslateY * fact)
            x.setTranslateZ(x.getTranslateZ * fact)
            x.setScaleX(x.getScaleX * fact)
            x.setScaleY(x.getScaleY * fact)
            x.setScaleZ(x.getScaleZ * fact)
            scaleShapes(tail)
          }
        }
      }

      //TODO - renomear função
      def aux[A](oct: Octree[Placement]): Octree[Placement] = {
        oct match {
          case OcEmpty => OcEmpty
          case OcLeaf(section) => {
            val sec = section.asInstanceOf[Section]
            val newSection = ((sec._1._1._1 * fact, sec._1._1._2 * fact, sec._1._1._3 * fact, sec._1._2 * fact), sec._2)
            //scaleShapes(section.asInstanceOf[Section]._2.asInstanceOf[List[Shape3D]])
            OcLeaf(newSection)
          }
          case OcNode(coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) => {
            val newCoords = ((coords._1._1 * fact, coords._1._2 * fact, coords._1._3 * fact), coords._2 * fact)
            OcNode(newCoords, aux(up_00), aux(up_01), aux(up_10), aux(up_11), aux(down_00), aux(down_01), aux(down_10), aux(down_11))
          }
        }
      }

      scaleShapes(getAllShapes())
      aux(octree)
      changeColor(getAllShapes())
    }

    def writeToFile(file: String, oct1: Octree[Placement]) = {
      val writer = new PrintWriter(new File(file))
      writer.write(oct1.toString)
      writer.close
    }

    //Mouse left click interaction
    scene.setOnMouseClicked((event) => {
      camVolume.setTranslateX(camVolume.getTranslateX + 2)
      changeColor(getAllShapes())
      writeToFile("output.txt", octree)
    })

    //TODO - apenas para testar o scaleOctree
    scene.setOnKeyPressed(k => {
      if (k.getCode == KeyCode.UP)
        scaleOctree(2, octree)
      else if (k.getCode() == KeyCode.DOWN)
        scaleOctree(0.5, octree)
    })

    //T5
    def mapColourEffect(func: Color => Color, oct: Octree[Placement]): Octree[Placement] = {
      oct match {
        case OcEmpty => OcEmpty
        case OcLeaf(section) => {
          val list = section.asInstanceOf[Section]._2
          list.map(x => {
            val c = x.asInstanceOf[Shape3D].getMaterial.asInstanceOf[PhongMaterial].getDiffuseColor
            val pm = new PhongMaterial()
            pm.setDiffuseColor(func(c))
            x.asInstanceOf[Shape3D].setMaterial(pm)
          })
          val section2: Section = (section.asInstanceOf[Section]._1, list)
          OcLeaf(section2)
        }
        case OcNode(coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) => {
          OcNode(coords, mapColourEffect(func, up_00), mapColourEffect(func, up_01), mapColourEffect(func, up_10), mapColourEffect(func, up_11),
            mapColourEffect(func, down_00), mapColourEffect(func, down_01), mapColourEffect(func, down_10), mapColourEffect(func, down_11))
        }
      }
    }

    //oct1 - example of an main.scala.ppm.Octree[Placement] that contains only one Node (i.e. cylinder1)
    //In case of difficulties to implement task T2 this octree can be used as input for tasks T3, T4 and T5

    //val placement1: Placement = ((0, 0, 0), 8.0)
    //val sec1: Section = (((0.0,0.0,0.0), 4.0), List(cylinder1.asInstanceOf[Node]))
    //val ocLeaf1 = main.scala.ppm.OcLeaf(sec1)
    //val oct1:main.scala.ppm.Octree[Placement] = main.scala.ppm.OcNode[Placement](placement1, ocLeaf1, main.scala.ppm.OcEmpty, main.scala.ppm.OcEmpty, main.scala.ppm.OcEmpty, main.scala.ppm.OcEmpty, main.scala.ppm.OcEmpty, main.scala.ppm.OcEmpty, main.scala.ppm.OcEmpty)

    /*
    //example of bounding boxes (corresponding to the octree oct1) added manually to the world
    val b2 = new Box(8,8,8)
    //translate because it is added by defaut to the coords (0,0,0)
    b2.setTranslateX(8/2)
    b2.setTranslateY(8/2)
    b2.setTranslateZ(8/2)
    b2.setMaterial(redMaterial)
    b2.setDrawMode(DrawMode.LINE)

    val b3 = new Box(4,4,4)
    //translate because it is added by defaut to the coords (0,0,0)
    b3.setTranslateX(4/2)
    b3.setTranslateY(4/2)
    b3.setTranslateZ(4/2)
    b3.setMaterial(redMaterial)
    b3.setDrawMode(DrawMode.LINE)

    //adding boxes b2 and b3 to the world
    worldRoot.getChildren.add(b2)
    worldRoot.getChildren.add(b3)
*/
  }

  override def init(): Unit = {
    println("init")
  }

  override def stop(): Unit = {
    println("stopped")
  }

  def sepia(color: Color):Color={
    val r = Math.min(((color.getRed * 0.4) + (color.getGreen * 0.77) + (color.getBlue * 0.2)).asInstanceOf[Int], 255)
    val g = Math.min(((color.getRed * 0.35) + (color.getGreen * 0.69) + (color.getBlue * 0.17)).asInstanceOf[Int], 255)
    val b = Math.min(((color.getRed * 0.27) + (color.getGreen * 0.53) + (color.getBlue * 0.13)).asInstanceOf[Int], 255)
    Color.rgb(r, g, b)
  }

  def greenRemove(color: Color): Color ={
    Color.rgb(color.getRed.asInstanceOf[Int],0, color.getBlue.asInstanceOf[Int])
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
    println(s"Class: ${shape.getClass}, ${shape.getTranslateX}, ${shape.getTranslateY}, ${shape.getTranslateZ}, ${shape.getScaleX}, ${shape.getScaleY}, ${shape.getScaleZ}")
  }

}

object FxApp {

  def main(args: Array[String]): Unit = {
    Application.launch(classOf[Main], args: _*)
  }
}

