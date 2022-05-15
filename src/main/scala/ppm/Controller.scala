package main.scala.ppm

import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextField}
import javafx.scene.SubScene
import javafx.scene.layout.{GridPane, Pane}
import main.scala.ppm.ImpureLayer.{OUTPUT_FILE, writeToFile, mapColourEffect}
import main.scala.ppm.InitSubScene.{camVolume, worldRoot}
import main.scala.ppm.PureLayer._

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

  @FXML
  private var label1: Label = _

  @FXML
  private var labelInvalid: Label = _

  @FXML
  private var submitButton: Button = _

  @FXML
  private var text1: TextField = _

  @FXML
  private var submitPane: Pane = _

  @FXML
  private var gridPane: GridPane = _

  def onScaleUpButtonClick() = {
    octree = scaleOctree(2, octree, worldRoot)
    writeToFile(OUTPUT_FILE, octree, worldRoot)
  }

  def onScaleDownButtonClick() = {
    octree = scaleOctree(0.5, octree, worldRoot)
    writeToFile(OUTPUT_FILE, octree, worldRoot)
  }

  def onSepiaButtonClick() = {
    octree = mapColourEffect(sepia, octree)
    writeToFile(OUTPUT_FILE, octree, worldRoot)
  }

  def onRemoveGreenButtonClick() = {
    octree = mapColourEffect(greenRemove, octree)
    writeToFile(OUTPUT_FILE, octree, worldRoot)
  }

  def onUpButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateX - 2)
    getShapesWithChangedColor(getAllShapesFromRoot(worldRoot))
  }

  def onDownButtonClick() = {
    camVolume.setTranslateX(camVolume.getTranslateX + 2)
    getShapesWithChangedColor(getAllShapesFromRoot(worldRoot))
  }

  /**
   * Função seria para usar quando a opção de inserir a depth através da GUI estiver completa
   * @return
   */
  def onSubmitButtonClick() = {
    val opt = text1.getText
    opt.toIntOption match {
      case None => {
        labelInvalid.setVisible(true)
        onUpButtonClick()
      }
      case Some(x) => {
        if (x > 0 && x <= 32) {
          octree = createOcTree(1, worldRoot, getAllShapesFromRoot(worldRoot), x)
          changeVisibilityOfFields()
        }
        else {
          labelInvalid.setVisible(true)
          onUpButtonClick()
        }
      }
    }
  }

  def changeVisibilityOfFields() = {
    submitPane.setVisible(false)
    gridPane.setVisible(true)
    labelInvalid.setVisible(false)
    text1.setVisible(false)
    label1.setVisible(false)
    submitButton.setVisible(false)
  }

}
