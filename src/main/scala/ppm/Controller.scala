package main.scala.ppm

import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.SubScene
import main.scala.ppm.ImpureLayer.{OUTPUT_FILE, writeToFile}
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
  private var downButton: Button = _

  def onScaleUpButtonClick() = {
    octree = scaleOctree(2, octree, worldRoot)
    writeToFile(OUTPUT_FILE, octree)
  }

  def onScaleDownButtonClick() = {
    octree = scaleOctree(0.5, octree, worldRoot)
    writeToFile(OUTPUT_FILE, octree)
  }

  def onSepiaButtonClick() = {
    octree = mapColourEffect(sepia, octree)
    writeToFile(OUTPUT_FILE, octree)
  }

  def onRemoveGreenButtonClick() = {
    octree = mapColourEffect(greenRemove, octree)
    writeToFile(OUTPUT_FILE, octree)
  }

  def onUpButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateX - 2)
    changeColor(getAllShapesFromRoot(worldRoot))
  }

  def onDownButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateX + 2)
    changeColor(getAllShapesFromRoot(worldRoot))
  }
}
