package com.ztftrue.unzip;

import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.SortedMap;

public class HelloController {
    @FXML
    public Button selectFile;
    @FXML
    public Button unCompress;
    @FXML
    public Button selectDir;
    @FXML
    public Button compressFile;
    @FXML
    public Label fileDir;
    @FXML
    public TextField searchInput;
    @FXML
    public Label selectCode;
    @FXML
    private Label welcomeText;
    @FXML
    private ListView<String> listViewCharset;
    // https://stackoverflow.com/questions/13246211/how-to-get-stage-from-controller-during-initialization
    private File zipFile;
    ArrayList<String> arrayList;
    ArrayList<String> defaultArrayList;
    HostServices hostServices;
    SortedMap<String, Charset> charsetSortedMap;
    public static Charset charset = StandardCharsets.UTF_8;
    private String searchText;
    private boolean isInit = true;
    public boolean appRunning = true;
    boolean searching = false;
    final Thread searchThread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (isInit) {
                isInit = false;
                try {
                    synchronized (searchThread) {
                        searchThread.wait();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            while (appRunning) {
                try {
                    synchronized (searchThread) {
                        searchThread.wait(300);
                    }
                    searching = true;
                    executeSearch(searchText);
                    searching = false;
                    synchronized (searchThread) {
                        searchThread.wait();
                    }
                } catch (InterruptedException e) {
                    // when thread is not waiting
                    e.printStackTrace();
                }
            }

        }
    });


    public void setZipFile(File zipFile) {
        this.zipFile = zipFile;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public void initialize() {
        searchThread.start();
        dealAllCharset();
        searchInput.textProperty().addListener((obs, oldText, newText) -> {
            // do what you need with newText here, e.g.
            searching = false;
            searchText = newText;
            synchronized (searchThread) {
                searchThread.notify();
            }
        });
    }


    @FXML
    protected void onHelloButtonClick(ActionEvent event) {
        if (event.getSource() == selectFile) {
            zipFile = selectFile();
            if (zipFile != null) {
                fileDir.setText(zipFile.getPath());
            }
        } else if (event.getSource() == unCompress) {
            if (zipFile != null && zipFile.exists() && zipFile.isFile()) {
                try {
                    unCompressZip(zipFile);
                    welcomeText.setText("Success");
                } catch (IOException e) {
                    e.printStackTrace();
                    if (welcomeText != null) {
                        welcomeText.setText(e.getLocalizedMessage());
                    }
                }
            } else {
                welcomeText.setText("Please select zip file");
            }
        } else if (event.getSource() == selectDir) {
            welcomeText.setText("Feature not implemented");
        } else if (event.getSource() == compressFile) {
            welcomeText.setText("Feature not implemented");
        }
    }


    public static String getFilePath(String filePath) {
        String realPath = filePath.substring(0, filePath.lastIndexOf("."));
        String tempFilePath = realPath;
        int i = 1;
        while (Files.exists(Path.of(realPath))) {
            realPath = tempFilePath + "(" + i + ")";
            i++;
        }
        return realPath;
    }

    @SuppressWarnings("unused")
    public void handleMouseClick(MouseEvent mouseEvent) {
        String s = listViewCharset.getSelectionModel().getSelectedItem();
        if (s != null) {
            charset = charsetSortedMap.get(s);
            selectCode.setText(s);
        }
    }

    public void openLink(ActionEvent actionEvent) {
        hostServices.showDocument(((Hyperlink) actionEvent.getSource()).getText());
    }

    protected File selectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Stage stage = (Stage) welcomeText.getScene().getWindow();
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("ZIP File", "*.zip")
        );
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        return fileChooser.showOpenDialog(stage);

    }

    public static void unCompressZip(File file) throws IOException {
        if (file != null && file.exists() && file.isFile()) {
            String filePath = file.getPath();
            String realPath = getFilePath(filePath);
            ZIP.UnZipFolder(file, realPath, charset);
        }
    }

    public void dealAllCharset() {
        charsetSortedMap = Charset.availableCharsets();
        listViewCharset.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        arrayList = new ArrayList<>(charsetSortedMap.size());
        for (Map.Entry<String, Charset> entry : charsetSortedMap.entrySet()) {
            String s = entry.getKey();
            if (s.equals("UTF-8") || s.startsWith("GB")) {
                arrayList.add(0, s);
            } else {
                arrayList.add(s);
            }
        }
        defaultArrayList = new ArrayList<>(arrayList);
        int i = arrayList.indexOf("UTF-8");
        updateListView(i);
    }

    public void updateListView(int defaultSelected) {
        listViewCharset.getItems().clear();
        listViewCharset.getItems().addAll(arrayList);
        if (defaultSelected >= 0 && defaultSelected < arrayList.size()) {
            listViewCharset.scrollTo(defaultSelected);
            listViewCharset.getSelectionModel().select(defaultSelected);
            listViewCharset.getFocusModel().focus(defaultSelected);
            charset = charsetSortedMap.get(arrayList.get(defaultSelected));
            selectCode.setText(arrayList.get(defaultSelected));
        }
    }

    public void executeSearch(String search) {
        arrayList.clear();
        search = search.toLowerCase();
        for (String s : defaultArrayList) {
            if (searching && s.toLowerCase().contains(search)) {
                arrayList.add(s);
            }
        }
        if (searching) {
            Platform.runLater(() -> updateListView(0));
        }
        searching = false;
    }
}