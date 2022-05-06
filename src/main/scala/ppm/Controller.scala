import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.SubScene

class Controller {

  @FXML
  private var subScene1:SubScene = _

  //method automatically invoked after the @FXML fields have been injected
  @FXML
  def initialize(): Unit = {
    InitSubScene.subScene.widthProperty.bind(subScene1.widthProperty)
    InitSubScene.subScene.heightProperty.bind(subScene1.heightProperty)
    subScene1.setRoot(InitSubScene.root)
  }
}
