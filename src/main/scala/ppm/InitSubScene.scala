import javafx.geometry.{Insets, Pos}
import javafx.scene.{Group, PerspectiveCamera, SceneAntialiasing, SubScene}
import javafx.scene.layout.StackPane
import javafx.scene.paint.{Color, PhongMaterial}
import javafx.scene.shape.{Cylinder, DrawMode, Line}
import javafx.scene.transform.Rotate

object InitSubScene{

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

  val root = new StackPane(subScene,cameraView)

  root.setOnMouseClicked((event) => {
    camVolume.setTranslateX(camVolume.getTranslateX + 2)
  })
}

