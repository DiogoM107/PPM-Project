package main.scala.ppm

import javafx.fxml.FXMLLoader
import javafx.geometry.{Insets, Pos}
import javafx.scene.{Group, Parent, PerspectiveCamera, Scene, SceneAntialiasing, SubScene}
import javafx.scene.layout.StackPane
import javafx.scene.paint.{Color, PhongMaterial}
import javafx.scene.shape.{Box, Cylinder, DrawMode, Line}
import javafx.scene.transform.Rotate

object InitSubScene{

  val blackMaterial = new PhongMaterial()
  blackMaterial.setDiffuseColor(Color.rgb(0, 0, 0))

  val whiteMaterial = new PhongMaterial()
  whiteMaterial.setDiffuseColor(Color.rgb(255, 255, 255))

  //Materials to be applied to the 3D objects
  val redMaterial = new PhongMaterial()
  redMaterial.setDiffuseColor(Color.rgb(150,0,0))

  val greenMaterial = new PhongMaterial()
  greenMaterial.setDiffuseColor(Color.rgb(0,255,255))

  val blueMaterial = new PhongMaterial()
  blueMaterial.setDiffuseColor(Color.rgb(0,0,150))

  //3D objects
  val lineX = new Line(0, 0, 200, 0)
  lineX.setStroke(Color.GREEN)

  val lineY = new Line(0, 0, 0, 200)
  lineY.setStroke(Color.YELLOW)

  val lineZ = new Line(0, 0, 200, 0)
  lineZ.setStroke(Color.LIGHTSALMON)
  lineZ.getTransforms.add(new Rotate(-90, 0, 0, 0, Rotate.Y_AXIS))

  val camVolume = new Cylinder(10, 50, 10)
  camVolume.setTranslateX(1)
  camVolume.getTransforms.add(new Rotate(45, 0, 0, 0, Rotate.X_AXIS))
  camVolume.setMaterial(blueMaterial)
  camVolume.setDrawMode(DrawMode.LINE)

  val worldRoot:Group = new Group(camVolume, lineX, lineY, lineZ)

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

  val subScene = new SubScene(worldRoot,200,200,true,SceneAntialiasing.BALANCED)
  subScene.setFill(Color.DARKSLATEGRAY)
  subScene.setCamera(camera)

  val cameraView = new CameraView(subScene)
  cameraView.setFirstPersonNavigationEabled(true)
  cameraView.setFitWidth(350)
  cameraView.setFitHeight(225)
  cameraView.getRx.setAngle(-45)
  cameraView.getT.setZ(-100)
  cameraView.getT.setY(-500)
  cameraView.getCamera.setTranslateZ(-50)
  cameraView.startViewing

  StackPane.setAlignment(cameraView, Pos.BOTTOM_RIGHT)
  StackPane.setMargin(cameraView, new Insets(5))

  val fxmlLoader = new FXMLLoader(getClass.getResource("Controller.fxml"))
  val controllerScene: Parent = fxmlLoader.load()

  val root = new StackPane(subScene,controllerScene,cameraView)
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

  subScene.widthProperty.bind(root.widthProperty)
  subScene.heightProperty.bind(root.heightProperty)

  val scene = new Scene(root, 810, 610, true, SceneAntialiasing.BALANCED)
}

