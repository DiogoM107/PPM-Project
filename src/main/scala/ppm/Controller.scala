package main.scala.ppm

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.SubScene
import main.scala.ppm.InitSubScene.{camVolume, worldRoot}
import main.scala.ppm.PureLayer.{changeColor, getAllShapesFromRoot, greenRemove, mapColourEffect, octree, scaleOctree, sepia}

class Controller {

  @FXML
  private var subScene1:SubScene = _

  @FXML
  private var scaleUpButton: Button = _

  @FXML
  private var scaleDownButton: Button = _

  @FXML
  private var sepiaButton: Button = _

  @FXML
  private var removeGreenButton: Button = _

  @FXML
  private var upButton: Button = _

  @FXML
  private var leftButton: Button = _

  @FXML
  private var rightButton: Button = _

  @FXML
  private var downButton: Button = _

  def onScaleUpButtonClick() = {
    octree = scaleOctree(2, octree, worldRoot)
  }

  def onScaleDownButtonClick() = {
    octree = scaleOctree(0.5, octree, worldRoot)
  }

  def onSepiaButtonClick() = {
    octree = mapColourEffect(sepia, octree)
  }

  def onRemoveGreenButtonClick() = {
    octree = mapColourEffect(greenRemove, octree)
  }

  def onUpButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateX - 2)
    changeColor(getAllShapesFromRoot(worldRoot))
  }

  def onDownButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateX + 2)
    changeColor(getAllShapesFromRoot(worldRoot))
  }

  def onLeftButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateY - 2)
    changeColor(getAllShapesFromRoot(worldRoot))
  }

  def onRightButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateY + 2)
    changeColor(getAllShapesFromRoot(worldRoot))
  }
}
