package main.scala.ppm

import javafx.scene.{Group, Node}
import javafx.scene.paint.{Color, PhongMaterial}
import javafx.scene.shape.{Box, Cylinder, DrawMode, Shape3D}
import main.scala.ppm.InitSubScene.{blueMaterial, camVolume, whiteMaterial}

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
  type Section = (Placement, List[Node]) //example: ( ((0.0,0.0,0.0), 2.0), List(new Cylinder(0.5, 1, 10)))


  //T2 - criar uma OcTree (Falta saber como definir o MIN_SCALE
  def createOcTree(scale: Double, root: Group, shapesList: List[Shape3D]): Octree[Placement] = {
    val placement: Placement = ((0, 0, 0), scale)
    createOcNode(placement, root, shapesList)
  }

  //TODO - verificar como fazer quando shape está entre dois leafs
  // Talvez enviar a lista dos shapes como argumento na funcao containsAShape,
  // e essa funcao devolve uma nova lista de shapes sem a(s) que foi(foram) inserida(s)
  def createOcNode(placement: Placement, root: Group, shapesList: List[Shape3D]): Octree[Placement] = {
    val xyz = placement._1
    val size = placement._2 / 2
    val nodeBox = createShapeCube(placement)
    if (containsAShape(nodeBox, root, shapesList)) {

      root.getChildren.add(nodeBox)
      if (placement._2 > MIN_SCALE) {
        OcNode(placement,
          createOcNode(((xyz._1, xyz._2 + size, xyz._3), (size)), root, shapesList),
          createOcNode(((xyz._1 + size, xyz._2 + size, xyz._3), (size)), root, shapesList),
          createOcNode(((xyz._1, xyz._2 + size, xyz._3 + size), (size)), root, shapesList),
          createOcNode(((xyz._1 + size, xyz._2 + size, xyz._3 + size), (size)), root, shapesList),
          createOcNode((xyz, (size)), root, shapesList),
          createOcNode(((xyz._1 + size, xyz._2, xyz._3), (size)), root, shapesList),
          createOcNode(((xyz._1, xyz._2, xyz._3 + size), (size)), root, shapesList),
          createOcNode(((xyz._1 + size, xyz._2, xyz._3 + size), (size)), root, shapesList)
        )
      }
      else {
        val newSection: Section = (placement, getContainedShapes(nodeBox, root, shapesList))
        OcLeaf(newSection)
      }

    }
    else OcEmpty
  }

  def getContainedShapes(node: Node, root: Group, shapesList: List[Shape3D]): List[Shape3D] = {
    def checkIfNodeContainsAShapeFromList(node: Node, list: List[Shape3D], contained: List[Shape3D]): List[Shape3D] = {
      list match {
        case Nil => contained
        case x :: xs => {
          if (node.getBoundsInParent.contains(x.asInstanceOf[Shape3D].getBoundsInParent)) {
            checkIfNodeContainsAShapeFromList(node, xs, x :: contained)
          }
          else {
            checkIfNodeContainsAShapeFromList(node, xs, contained)
          }
        }
      }
    }

    checkIfNodeContainsAShapeFromList(node, shapesList, Nil)
  }

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

  def containsAShape(node: Node, root: Group, shapesList: List[Shape3D]): Boolean = {
    def checkIfNodeContainsShape(node: Node, list: List[Shape3D]): Boolean = {
      list match {
        case Nil => false
        case x :: xs => {
          if (node.getBoundsInParent.contains(x.asInstanceOf[Shape3D].getBoundsInParent)) {
            true
          }
          else {
            checkIfNodeContainsShape(node, xs)
          }
        }
      }
    }

    checkIfNodeContainsShape(node, shapesList)
  }

  //T1
  def getShapesFromList(lst: List[String], root: Group): List[Shape3D] = {
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
            cylinder :: getShapesFromList(tail, root)
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
            box :: getShapesFromList(tail, root)

          }
          case _ => {
            getShapesFromList(tail, root)
          }
        }
      }
    }

  }

  def getAllShapesFromRoot(root: Group): List[Shape3D] = {
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

    val list = root.getChildren.asScala.toList
    aux(list, Nil)
  }

  //TODO - deve enviar uma lista com os objetos novos e não alterar diretamente
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

  //T1
  def createShapesFromFile(file: String, root: Group): List[Shape3D] = {
    val lines = Source.fromFile(file).getLines().toList
    getShapesFromList(lines, root)
  }

  //TODO - verificar como fazer
  def getOcTreeShapes(oct: Octree[Placement]): List[Node] = {
    def getListOfObjects(oct2: Octree[Placement]): List[Object] = {
      oct2 match {
        case OcEmpty => Nil
        case OcLeaf(section) => {
          section.asInstanceOf[Section]._2
        }
        case OcNode(coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) => {
          getListOfObjects(up_00) :: getListOfObjects(up_01) :: getListOfObjects(up_10) :: getListOfObjects(up_11) :: getListOfObjects(down_00) :: getListOfObjects(down_01) :: getListOfObjects(down_10) :: getListOfObjects(down_11)
        }
      }
    }

    def filterListOfObjects(objs: List[Object], shapes: List[Shape3D]): List[Shape3D] = {
      objs match {
        case Nil => shapes
        case x :: tail => {
          if (x.isInstanceOf[Node]) {
            filterListOfObjects(tail, x.asInstanceOf[Shape3D] :: shapes)
          }
          else {
            filterListOfObjects(tail, shapes)
          }
        }
      }
    }

    filterListOfObjects(getListOfObjects(oct), Nil)
  }


  //T4 - deve devolver a ocTree
  def scaleOctree(fact: Double, oct: Octree[Placement], root: Group): Octree[Placement] = {
    //TODO - deve devolver a lista de objetos nova e nao alterar diretamente
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

    //TODO - deve devolver uma nova octree sem alterar a antiga
    def scaleOctNodes[A](oct: Octree[Placement]): Octree[Placement] = {
      oct match {
        case OcEmpty => OcEmpty
        case OcLeaf(section) => {
          val sec = section.asInstanceOf[Section]
          val newSection: Section = (((sec._1._1._1 * fact, sec._1._1._2 * fact, sec._1._1._3 * fact), sec._1._2 * fact), sec._2)
          //scaleShapes(section.asInstanceOf[Section]._2.asInstanceOf[List[Shape3D]])
          OcLeaf(newSection)
        }
        case OcNode(coords, up_00, up_01, up_10, up_11, down_00, down_01, down_10, down_11) => {
          val newCoords = ((coords._1._1 * fact, coords._1._2 * fact, coords._1._3 * fact), coords._2 * fact)
          OcNode(newCoords, scaleOctNodes(up_00), scaleOctNodes(up_01), scaleOctNodes(up_10), scaleOctNodes(up_11), scaleOctNodes(down_00), scaleOctNodes(down_01), scaleOctNodes(down_10), scaleOctNodes(down_11))
        }
      }
    }

    //TODO - devia estar a ir buscar os objetos da ocTree e não do root
    scaleShapes(getAllShapesFromRoot(root))
    var oct2 = scaleOctNodes(oct)
    changeColor(getAllShapesFromRoot(root))
    oct2
  }


  //TODO - deve devolver uma nova octree sem alterar a antiga
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

  def sepia(color: Color): Color = {
    val r = Math.min((((color.getRed * 0.4 * 255)) + ((color.getGreen * 0.77 * 255)) + ((color.getBlue * 0.2 * 255))).asInstanceOf[Int], 255)
    val g = Math.min((((color.getRed * 0.35 * 255)) + ((color.getGreen * 0.69 * 255)) + ((color.getBlue * 0.17 * 255))).asInstanceOf[Int], 255)
    val b = Math.min((((color.getRed * 0.27 * 255)) + ((color.getGreen * 0.53 * 255)) + ((color.getBlue * 0.13 * 255))).asInstanceOf[Int], 255)
    Color.rgb(r, g, b)
  }

  def greenRemove(color: Color): Color = {
    Color.rgb((color.getRed * 255).asInstanceOf[Int], 0, (color.getBlue * 255).asInstanceOf[Int])
  }


}
