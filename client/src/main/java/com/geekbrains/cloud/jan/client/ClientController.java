package com.geekbrains.cloud.jan.client;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.geekbrains.cloud.jan.model.FileMessage;
import com.geekbrains.cloud.jan.model.FileRequest;
import com.geekbrains.cloud.jan.model.FilesList;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

public class ClientController implements Initializable {

    private Net network;
    @FXML
    public TableView<FileInfo> clientView;
    public TableView<FileInfo> serverView;
    public TextField textField;
    public TextField textFieldS;
    public ComboBox<String> disksBox; // перечисление дисков
    public ComboBox<String> disksBoxS;
    public Label clientLabel;
    public Label serverLabel;

    private Path currentDir;
    private Net net;
    // sync mode
    // recommended mode
    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    /*private void fillCurrentDirFiles() {
        Platform.runLater(() -> {
            clientView.getItems().clear();
            clientView.getItems().add("..");
            clientView.getItems().addAll(currentDir.toFile().list());
            clientLabel.setText(getClientFilesDetails());
        });
    }

    private String getClientFilesDetails() {
        File[] files = currentDir.toFile().listFiles();
        long size = 0;
        String label;
        if (files != null) {
            label = files.length + " files in current dir. ";
            for (File file : files) {
                size += file.length();
            }
            label += "Summary size: " + size + " bytes.";
        } else {
            label = "Current dir is empty";
        }
        return label;
    }

    private void initClickListener() {
        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String fileName = clientView.getSelectionModel().getSelectedItem();
                System.out.println("Выбран файл: " + fileName);
                Path path = currentDir.resolve(fileName);
                if (Files.isDirectory(path)) {
                    currentDir = path;
                    fillCurrentDirFiles();
                }
            }
        });
    }*/

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //создаем колонки на клиенте
        //в таблице будет зраниться файл инфи и будет преобразована к строке
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumn.setPrefWidth(24);// по умолчанию ширина столбца

        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumn.setPrefWidth(240);// по умолчанию ширина столбца

        TableColumn<FileInfo, Long> filesSizeColumn = new TableColumn<>("Size");
        ;
        filesSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        filesSizeColumn.setPrefWidth(120);// по умолчанию ширина столбца
        filesSizeColumn.setCellFactory(column -> {
            // как выглядит ячейка с столбце
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    //если лонг не запонен или ячейка пустая, тогда в нее не записываем
                    if (item == null || empty) {
                        setText("");
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);// размер с пробелами
                        // если размер файла -1
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Date");
        fileDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);// по умолчанию ширина столбца
        // на клиенте добавили и отсортировали колонки
        clientView.getColumns().addAll(fileTypeColumn, fileNameColumn, filesSizeColumn, fileDateColumn);
        clientView.getSortOrder().add(fileTypeColumn);

        //создаем колонки на сервере
        //в таблице будет зраниться файл инфи и будет преобразована к строке
        TableColumn<FileInfo, String> fileTypeColumnS = new TableColumn<>();
        fileTypeColumnS.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType().getName()));
        fileTypeColumnS.setPrefWidth(24);// по умолчанию ширина столбца

        TableColumn<FileInfo, String> fileNameColumnS = new TableColumn<>("Name");
        fileNameColumnS.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFilename()));
        fileNameColumnS.setPrefWidth(240);// по умолчанию ширина столбца

        TableColumn<FileInfo, Long> filesSizeColumnS = new TableColumn<>("Size");
        filesSizeColumnS.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSize()));
        filesSizeColumnS.setPrefWidth(120);// по умолчанию ширина столбца
        filesSizeColumnS.setCellFactory(column -> {
            // как выглядит ячейка с столбце
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item, empty);
                    //если лонг не запонен или ячейка пустая, тогда в нее не записываем
                    if (item == null || empty) {
                        setText("");
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes", item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });
        DateTimeFormatter dtfS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumnS = new TableColumn<>("Date");
        fileDateColumnS.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModified().format(dtfS)));
        fileDateColumnS.setPrefWidth(120);// по умолчанию ширина столбца
        // на клиенте добавили и отсортировали колонки
        serverView.getColumns().addAll(fileTypeColumnS, fileNameColumnS, filesSizeColumnS, fileDateColumnS);
        serverView.getSortOrder().add(fileTypeColumnS);

        disksBox.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBox.getItems().add(p.toString());
        }
        disksBox.getSelectionModel().select(0);

        disksBoxS.getItems().clear();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            disksBoxS.getItems().add(p.toString());
        }
        disksBoxS.getSelectionModel().select(0);
        // первоначальное заполнение, при открытии таблиц
        updateList(Paths.get("."));
        updateListS(Paths.get(".", "serverDir"));
        clientView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(textField.getText()).resolve(clientView.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateList(path);
                    }
                }
            }
        });

        serverView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Path path = Paths.get(textFieldS.getText()).resolve(serverView.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateListS(path);
                    }
                }
            }
        });
        /*try {
            currentDir = Paths.get(System.getProperty("user.home"));
            fillCurrentDirFiles();
            initClickListener();
            net = Net.getInstance();
            net.setCallback(message -> {
                switch (message.getType()) {
                    case FILE_MESSAGE:
                        FileMessage fileMessage = (FileMessage) message;
                        Files.write(currentDir.resolve(fileMessage.getFileName()), fileMessage.getBytes());
                        fillCurrentDirFiles();
                        break;
                    case LIST:
                        FilesList list = (FilesList) message;
                        updateServerView(list.getList());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void updateList(Path path) {

        try {
            //  наполним списком файлов которые есть куда указан любой путь
            textField.setText(path.normalize().toAbsolutePath().toString());
            clientView.getItems().clear();
            clientView.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            clientView.sort();

        } catch (IOException e) {
            // Всплывающее окно. И подождать пока пользователь нажмет на ок
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов клиента", ButtonType.OK);
            alert.showAndWait();

        }
    }

    public void updateListS(Path path) {

        try {
            //  наполним списком файлов которые есть куда указан любой путь

            textFieldS.setText(path.normalize().toAbsolutePath().toString());
            serverView.getItems().clear();
            serverView.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            serverView.sort();

        } catch (IOException e) {
            // Всплывающее окно. И подождать пока пользователь нажмет на ок
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов сервера", ButtonType.OK);
            alert.showAndWait();

        }
    }

    public void exit(ActionEvent actionEvent) {
        Platform.exit();
    }

    // переход к родителю, кн Вверх
    public void btnPathUpAction(ActionEvent actionEvent) {
        Path upperPath = Paths.get(textField.getText()).getParent();//путь вернет родителя
        if (upperPath != null) {
            updateList(upperPath);
        }
    }

    public void btnPathUpActionS(ActionEvent actionEvent) {
        Path upperPath = Paths.get(textFieldS.getText()).getParent();//путь вернет родителя
        if (upperPath != null) {
            updateListS(upperPath);
        }
    }

    // при выборе диска, переходим в его корень
    public void selectionDiskAction(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateList(Paths.get(element.getSelectionModel().getSelectedItem()));

    }

    public void selectionDiskActionS(ActionEvent actionEvent) {
        ComboBox<String> element = (ComboBox<String>) actionEvent.getSource();
        updateListS(Paths.get(element.getSelectionModel().getSelectedItem()));

    }

    // проверка на активную таблицу, вернет имя и путь выделенного файла
    public String getSelectedFileName() {
        if (clientView.isFocused()) {
            return clientView.getSelectionModel().getSelectedItem().getFilename();

        } else if (serverView.isFocused()) {
            return serverView.getSelectionModel().getSelectedItem().getFilename();
        }
        return null;
    }

    public String getCurrentPath() {
        return textField.getText();
    }

    public String getCurrentPathS() {
        return textFieldS.getText();
    }

    private void updateServerView(List<String> names) {
        /*Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(names);
        });*/
    }


    public void downLoad(ActionEvent actionEvent) {
        if (getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, " Ни один файл не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (clientView.isFocused()) {
            Path srcPath = Paths.get(getCurrentPath(), getSelectedFileName());// формируем путь к файлу откуда будем копировать
            Path dstPath = Paths.get(getCurrentPathS()).resolve(srcPath.getFileName());// скопируем в корневой каталог с именем как с клиента
            try {
                Files.move(srcPath, dstPath);
                updateListS(Paths.get(getCurrentPathS()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " Не удалось скопировать файл", ButtonType.OK);
                alert.showAndWait();
            }

        } else {
            Path srcPath = Paths.get(getCurrentPathS(), getSelectedFileName());// формируем путь к файлу откуда будем копировать
            Path dstPath = Paths.get(getCurrentPath()).resolve(srcPath.getFileName());// скопируем в корневой каталог с именем как с клиента
            try {
                Files.move(srcPath, dstPath);
                updateList(Paths.get(getCurrentPath()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " Не удалось скопировать файл", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }


    public void upLoad(ActionEvent actionEvent) {
        if (getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, " Ни один файл не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (clientView.isFocused()) {
            Path srcPath = Paths.get(getCurrentPath(), getSelectedFileName());// формируем путь к файлу откуда будем копировать
            Path dstPath = Paths.get(getCurrentPathS()).resolve(srcPath.getFileName());// скопируем в корневой каталог с именем как с клиента
            try {
                Files.move(srcPath, dstPath);
                updateListS(Paths.get(getCurrentPathS()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " Не удалось переместить файл", ButtonType.OK);
                alert.showAndWait();
            }

        } else {
            Path srcPath = Paths.get(getCurrentPathS(), getSelectedFileName());// формируем путь к файлу откуда будем копировать
            Path dstPath = Paths.get(getCurrentPath()).resolve(srcPath.getFileName());// скопируем в корневой каталог с именем как с клиента
            try {
                Files.move(srcPath, dstPath);
                updateList(Paths.get(getCurrentPath()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " Не удалось переместить файл", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }

    public void copyBtn(ActionEvent actionEvent) {
        if (getSelectedFileName() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, " Ни один файл не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (clientView.isFocused()) {
            Path srcPath = Paths.get(getCurrentPath(), getSelectedFileName());// формируем путь к файлу откуда будем копировать
            Path dstPath = Paths.get(getCurrentPathS()).resolve(srcPath.getFileName());// скопируем в корневой каталог с именем как с клиента
            try {
                Files.copy(srcPath, dstPath);
                updateListS(Paths.get(getCurrentPathS()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " Не удалось скопировать файл", ButtonType.OK);
                alert.showAndWait();
            }

        } else {
            Path srcPath = Paths.get(getCurrentPathS(), getSelectedFileName());// формируем путь к файлу откуда будем копировать
            Path dstPath = Paths.get(getCurrentPath()).resolve(srcPath.getFileName());// скопируем в корневой каталог с именем как с клиента
            try {
                Files.copy(srcPath, dstPath);
                updateList(Paths.get(getCurrentPath()));
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, " Не удалось скопировать файл", ButtonType.OK);
                alert.showAndWait();
            }
        }
    }
}
