package com.rameysoft.kewos.controllers;


import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.controlsfx.control.ButtonBar.ButtonType;
import org.controlsfx.control.StatusBar;
import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.DialogAction;
import org.controlsfx.dialog.Dialogs;

import com.panemu.tiwulfx.common.TableCriteria;
import com.panemu.tiwulfx.common.TableData;
import com.panemu.tiwulfx.table.ButtonColumn;
import com.panemu.tiwulfx.table.ButtonColumnController;
import com.panemu.tiwulfx.table.TableControl;
import com.panemu.tiwulfx.table.TableController;
import com.panemu.tiwulfx.table.TextColumn;
import com.rameysoft.kewos.search.SearchFactory;
import com.rameysoft.kewos.search.SearchFactory.SearchType;
import com.rameysoft.kewos.search.SearchFactory.SearcherI;
import com.rameysoft.kewos.search.model.Result;

@SuppressWarnings("deprecation")
public class MainController implements ButtonColumnController<Result>{

	private StatusBar statusbar;
    @FXML    private Button searchButton;
    @FXML    private Button clearTableBtn;
    @FXML    private Button browseFileSavingPath;
    @FXML    private Button searchButtonCustom;
    @FXML    private Button copyFiles;
    @FXML    private ResourceBundle resources;
    @FXML    private URL location;    
    @FXML    private AnchorPane appMain;
    @FXML    private TextField destExcelFilePath;
    @FXML    private ListView<File> pathToPdbFiles;
    @FXML    private AnchorPane tableContainer;    
    @FXML    protected TableControl<Result> tblResults;
    @FXML    protected TextField keyWordText ;
    @FXML    protected TextField filesSavingPath;
    @FXML    private TabPane tabPane;
    
		  private ButtonColumn<Result> clmFileName;
	@FXML private TextColumn<Result> clmMISSING_RESIDUES;
	@FXML private TextColumn<Result> clmMUTATION;
	@FXML private TextColumn<Result> clmEC;
	@FXML private TextColumn<Result> clmEXPDTA;
	@FXML private TextColumn<Result> clmCHAIN;
	@FXML private TextColumn<Result> clmMOLECULE;
	@FXML private TextColumn<Result> clmRESOLUTION;
	@FXML private TextColumn<Result> clmHetnam;
	@FXML private TextColumn<Result> clmPath;
	
	@FXML    private CheckBox ckbExpDTA;
    @FXML    private CheckBox ckbMissingRes;
    @FXML    private CheckBox ckbEc;
    @FXML    private CheckBox ckbMutation;
    @FXML    private CheckBox ckbResolution;
    @FXML    private CheckBox ckbChain;
    @FXML    private CheckBox ckbMolecult;
    @FXML    private CheckBox ckbHetnam;
    
    @FXML private Tab tab1;
    @FXML private Tab tab2;
    
    private SearchFactory mSearchFactory;
    private FileChooser  fileChooser;
    protected Tab selectedIndex = null;
    public List<Result> resultData = new ArrayList<>();
    private ObservableList<File> mFiles = FXCollections.observableArrayList();
    
	@FXML
    void initialize() {
		selectedIndex = tab1;
		pathToPdbFiles.setItems(mFiles);
		fileChooser = new FileChooser();

    	fileChooser.setTitle("Select PDB Files Directory");
    	fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home")));
    	fileChooser.getExtensionFilters().addAll(
    	         new FileChooser.ExtensionFilter("PDB Files", "*.pdb"),/*
    	         new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
    	         new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),*/
    	         new FileChooser.ExtensionFilter("All Files", "*.*"));
    	//initStatusBar();
    	tblResults.setRecordClass(Result.class);
    	clmFileName = new ButtonColumn<Result>("fileName", 100d, "FILE NAME");
    	clmFileName.setHelper(this);
    	tblResults.getColumns().add(0, clmFileName);
    	tblResults.getColumns().remove(clmPath);
    	tblResults.setController(controller);
    	tblResults.setMaxRecord(5000);
    	tblResults.reloadFirstPage();    	
    	tblResults.setVisibleComponents(false, 
    			TableControl.Component.BUTTON_DELETE,
    			TableControl.Component.BUTTON_EDIT,
    			TableControl.Component.BUTTON_INSERT,
    			TableControl.Component.BUTTON_RELOAD,
    			TableControl.Component.BUTTON_PAGINATION,
    			TableControl.Component.BUTTON_SAVE);
    	
    	tblResults.setDisableComponents(true,TableControl.Component.BUTTON_EXPORT);
    	
    	mSearchFactory = SearchFactory.getInstance();
    	//service result listener
    	service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
            	
            	if(!resultData.isEmpty()){     
        			tblResults.setDisableComponents(false,TableControl.Component.BUTTON_EXPORT);            			               	
            		tblResults.reloadFirstPage();
            	}
            }
        });
    	
    	serviceForCustomeSearch.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent t) {
            	
            	if(!resultData.isEmpty()){     
        			tblResults.setDisableComponents(true,TableControl.Component.BUTTON_EXPORT);            			               	
            		tblResults.reloadFirstPage();
            	}
            }
        });
		
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
		    @Override
		    public void changed(ObservableValue<? extends Tab> ov, Tab oldValue, Tab newValue) {
		    	selectedIndex = newValue;
		    	if(resultData.size() > 0){
		    		resultData.clear();
	    		}
		    	if(selectedIndex == tab1){
		    		
		    		serviceForCustomeSearch.cancel();
		    		service.reset();
		    		
		    		ckbChain.setSelected(true);
        	    	ckbEc.setSelected(true);
        	    	ckbExpDTA.setSelected(true);
        	    	ckbHetnam.setSelected(true);
        	    	ckbMissingRes.setSelected(true);
        	    	ckbMolecult.setSelected(true);
        	    	ckbMutation.setSelected(true);
        	    	ckbResolution.setSelected(true);
        	    	
        	    	tblResults.getColumns().clear();
        	    	tblResults.getColumns().add(0, clmFileName);
            		tblResults.getColumns().add(clmMISSING_RESIDUES);
            		tblResults.getColumns().add(clmMUTATION);
            		tblResults.getColumns().add(clmRESOLUTION);
            		tblResults.getColumns().add(clmEC);
            		tblResults.getColumns().add(clmEXPDTA);
            		tblResults.getColumns().add(clmCHAIN);
            		tblResults.getColumns().add(clmMOLECULE);
        	    	tblResults.getColumns().add(clmHetnam);

        	    	ckbChain.setDisable(false);
        	    	ckbEc.setDisable(false);
        	    	ckbExpDTA.setDisable(false);
        	    	ckbHetnam.setDisable(false);
        	    	ckbMissingRes.setDisable(false);
        	    	ckbMolecult.setDisable(false);
        	    	ckbMutation.setDisable(false);
        	    	ckbResolution.setDisable(false);
        	    	tblResults.reloadFirstPage();
        	    	
        	    	searchButton.setDisable(false);
            		            		
		    	} else {
		    		service.cancel();
		    		serviceForCustomeSearch.reset();		    		
		
		    		tblResults.getColumns().clear();
        			tblResults.getColumns().add(0, clmFileName);
        			tblResults.getColumns().add(1, clmPath);
        			searchButtonCustom.setDisable(false);
		    		disableKeyWordSelection(false);
		    	}
		    }
		});
		
	}   

    @FXML
    void onBrowseAction(ActionEvent event) {
    	
    	List<File> list =
                fileChooser.showOpenMultipleDialog(getStage());
        
        if(list != null && list.size()>0){          
        	mFiles.addAll(list);
        	
        } else {
        	Action yes = new DialogAction("Ok", ButtonType.YES);
			
			Action response = Dialogs.create()
	                .title("Text Search")
	                .masthead("Error")
	                .message("Select a proper directory")
	                .actions(yes)
	                .styleClass(Dialog.STYLE_CLASS_NATIVE)
	                .showWarning();
	        
 			if (response == yes) {
 			   return;
 			}
        }
    }
    
    @FXML
    void onDestBrowseAction(ActionEvent event) {
    	destExcelFilePath.setText(distinationPath());
    }
    
    @FXML
    void onBrowseActionFileSave(ActionEvent event) {
    	filesSavingPath.setText(distinationPath());
    }
    
    @FXML
    void onCopyFilesAction(ActionEvent event) {
    	String desDir = filesSavingPath.getText();
    	if(!desDir.isEmpty() ){
    		
    		if(resultData.size() > 0){
    			copyService.reset();
    			Dialogs.create()
                .owner(getStage())
                .title("Copy")
                .masthead("Copying the result Files")
                .showWorkerProgress(copyService);    	
    			copyService.start();	
    			
    		} else {
    			Dialogs.create()
                .title("Text Search")
                .masthead("Error")
                .message("Result Data is empty. Nothing to copy")
                .styleClass(Dialog.STYLE_CLASS_NATIVE)
                .showWarning();
    		}
    	} else {
			
			Dialogs.create()
	                .title("Text Search")
	                .masthead("Error")
	                .message("Select an existing folder")
	                .styleClass(Dialog.STYLE_CLASS_NATIVE)
	                .showWarning();
    	}
    }
	private String distinationPath() {
		DirectoryChooser directoryChooser = new DirectoryChooser();

        directoryChooser.setTitle("Distination Path");
        directoryChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
            ); 
        File selectedDirectory = 
                directoryChooser.showDialog(getStage());
         
         if(selectedDirectory != null){
             if(!selectedDirectory.getName().isEmpty()){
            	return selectedDirectory.getAbsolutePath();                 
             }
         } else {
         	Action yes = new DialogAction("Ok", ButtonType.YES);
			
 			Dialogs.create()
 	                .title("Text Search")
 	                .masthead("Error")
 	                .message("Select a proper directory")
 	                .actions(yes)
 	                .styleClass(Dialog.STYLE_CLASS_NATIVE)
 	                .showWarning();
         }
         
         return System.getProperty("user.home");
	}
    
    @FXML
    void onSearchAction(ActionEvent event) {
    	if(!mFiles.isEmpty()){   
    		Dialogs.create()
            .owner(getStage())
            .title("TextSearch")
            .masthead("Searching on given Files")
            .showWorkerProgress(service);    	
        	service.start();	
        	disableKeyWordSelection(true);
        	clearTableBtn.setDisable(false);
        	
    	} else {
    		Action ok = new DialogAction("Ok", ButtonType.YES);
			
			Dialogs.create()
                .title("Text Search")
                .masthead("Error")
                .message("Browse and Select one or more files")
                .actions(ok)
                .styleClass(Dialog.STYLE_CLASS_NATIVE)
                .showWarning();
    	}
    }
    
    @FXML
    void onSearchActionCustom(ActionEvent event) {
    	if(!mFiles.isEmpty()){    	
    		Dialogs.create()
            .owner(getStage())
            .title("TextSearch")
            .masthead("Searching on given Files")
            .showWorkerProgress(serviceForCustomeSearch);    	
    		serviceForCustomeSearch.start();	
        	disableKeyWordSelection(true);
        	clearTableBtn.setDisable(false);
        	
    	} else {
    		Action ok = new DialogAction("Ok", ButtonType.YES);
			
			Dialogs.create()
                .title("Text Search")
                .masthead("Error")
                .message("Browse and Select one or more files")
                .actions(ok)
                .styleClass(Dialog.STYLE_CLASS_NATIVE)
                .showWarning();
    	}
    }
    
    @SuppressWarnings("unused")
	private void initStatusBar() {
        statusbar = new StatusBar();
        statusbar.setText("");
        appMain.getChildren().add(statusbar);
        AnchorPane.setBottomAnchor(statusbar, 0d);
        AnchorPane.setLeftAnchor(statusbar, 0d);
        AnchorPane.setRightAnchor(statusbar, 0d);
    }
    
    public Stage getStage() {
        if(appMain != null && appMain.getScene() != null ){
            return (Stage) appMain.getScene().getWindow();
        } else {
            return null;
        }
    }

    protected Service<Void> copyService = new Service<Void>() {

		@Override
		protected Task<Void> createTask() {
			return new Task<Void>() {

				@Override
				protected Void call() throws Exception {
					  String desDir = filesSavingPath.getText();
		              int i=0;
		              int max = resultData.size();
		              updateMessage("Initializing Copy");                        	 
		              updateProgress(0, max);
		              
		              for(Result result : resultData){
		    				Path srcFile = Paths.get(result.getPath());
		    				Path dest = Paths.get(desDir,Paths.get(result.getFileName()).getFileName().toString());
		    				try {
								Files.move(srcFile, dest, StandardCopyOption.REPLACE_EXISTING);
							} catch (IOException e) {
								e.printStackTrace();
							}
		    				
		    				updateProgress(++i, max );
			     			int percent = (i*100)/max;
			     			 
			     			updateMessage("Searching ... (" +percent + " %)");
	    			  }  		    

		     		  updateMessage("Copy Complete");
		     		  return null;
				}
			};
		}
	};
    protected Service<List<Result>> service = new Service<List<Result>>() {

		@Override
		protected Task<List<Result>> createTask() {
			return new Task<List<Result>>() {

				@Override
				protected List<Result> call() throws Exception {
					
		              resultData.clear();
		              int i=0;
		              int max = mFiles.size();
		              updateMessage("Initializing Search");                        	 
		              updateProgress(0, max);
		              
		     		  for(File file : mFiles){
		     			  Path path = file.toPath();
		     			  FileChannel channel;
		     			  Result result = new Result();
		     			  result.setFileName(path.toString());
		     			  
		     			  try {
		     				channel = FileChannel.open(path, StandardOpenOption.READ);
		     				CharBuffer buf = StandardCharsets.UTF_8.decode(channel.map(MapMode.READ_ONLY, 0, channel.size()));
		     				channel.close();
		     				//--------------------------
		     				for(SearcherI searcher : SearchFactory.getInstance().getSearchAbles().values()){
		     					result = searcher.doSearch(result, buf);
		     				}		     				
		     				//--------------------------
		     				
		     				buf = null;
		     				//--------------------------
		     			  } catch (IOException e) {
		     				e.printStackTrace();
		     			  }
		     			
		     			  resultData.add(result);
		     			  updateProgress(++i, max );
		     			  int percent = (i*100)/max;
		     			 
		     			  updateMessage("Searching ... (" +percent + " %)");
		     		 }  

		     		  updateMessage("Search Complete");
		     		  return resultData;
				}
			};
		}
	};
	
	 protected Service<List<Result>> serviceForCustomeSearch = new Service<List<Result>>() {

			@Override
			protected Task<List<Result>> createTask() {
				return new Task<List<Result>>() {

					@Override
					protected List<Result> call() throws Exception {
						
			              resultData.clear();
			              int i=0;
			              int max = mFiles.size();
			              Pattern custome = null;
		            	  String text = keyWordText.getText().trim();
		            	  if(text != null && !text.isEmpty()){
			            	  custome = Pattern.compile("("+text+")");
				              
				              updateMessage("Initializing Search");                        	 
				              updateProgress(0, max);
				              
				     		  for(File file : mFiles){
				     			  Path path = file.toPath();
				     			  FileChannel channel;
				     			  
				     			  try {
				     				channel = FileChannel.open(path, StandardOpenOption.READ);
				     				CharBuffer buf = StandardCharsets.UTF_8.decode(channel.map(MapMode.READ_ONLY, 0, channel.size()));
				     				channel.close();
				     				//--------------------------
				     				Matcher matcher = custome.matcher(buf);
				     				if(matcher.find()){
				     					Result result = new Result();
						     			result.setFileName(path.toString());
						     			result.setPath(path.toString());
						     			resultData.add(result);
				     				}			     						     				
				     				//--------------------------
				     				
				     				buf = null;
				     				//--------------------------
				     			  } catch (IOException e) {
				     				e.printStackTrace();
				     			  }
				     			
				     			  updateProgress(++i, max );
				     			  int percent = (i*100)/max;
				     			 
				     			  updateMessage("Searching ... (" +percent + " %)");
				     		  }  
	
				     		  updateMessage("Search Complete");
		            	  } else {
		            		  updateMessage("Search Failed, empty search string");
		            	  }
			     		  return resultData;
					}
				};
			}
		};
    
    private TableController<Result> controller = new TableController<Result>() {
		
		@SuppressWarnings("rawtypes")
		@Override
		public TableData<Result> loadData(int startIndex, List<TableCriteria> filteredColumns,
				List<String> sortedColumns, List<SortType> sortingOrders, int maxResult) {
			return new TableData<Result>(resultData, false, resultData.size());
		}
		
		@SuppressWarnings("rawtypes")
		@Override
		public void exportToExcel(String title, int maxResult, TableControl<Result> tblView, List<TableCriteria> lstCriteria, String filePath) {
				super.exportToExcel("Search Results Data ", maxResult, tblView, lstCriteria, destExcelFilePath.getText());
			
		}
	};

	public void setStatusBarText(final String text){        
        Platform.runLater(() -> {
            statusbar.setText(text);
        });
        
    }

	@Override
	public void initButton(Hyperlink button, TableCell<Result, ?> row) {
		
	}

	@Override
	public void redrawButton(Hyperlink button, Result record, String text) {
		button.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				if (Desktop.isDesktopSupported()) {
	            	Desktop desktop = Desktop.getDesktop();
	                if (desktop.isSupported(Desktop.Action.OPEN)) {
	                	File file = new File(record.getFileName());
	                    try{
	                         desktop.open(file);                               
	                    } catch (IOException ioe) {
	                        System.out.println("Cannot perform the given operation to the " + file + " file");
	                    }
	                }
	            }
			}
		});
		button.setStyle("-fx-text-fill: black;");
		if(text!=null){
			Path p = Paths.get(text);
			button.setText(p.getFileName().toString());
		}
	}

	@FXML
	void onCloseAction(ActionEvent event) {
	    	System.exit(0);
	}

    @FXML
    void onAboutMenuItemAction(ActionEvent event) {

    }
    

    @FXML
    void onCkbMisResAction(ActionEvent event) {
    	if(ckbMissingRes.isSelected()){
    		tblResults.getColumns().add(clmMISSING_RESIDUES);
    		mSearchFactory.putSearchAble(SearchType.MISSING_RESIDUES);
    	} else {
    		tblResults.getColumns().remove(clmMISSING_RESIDUES);
    		mSearchFactory.removeSearchAble(SearchType.MISSING_RESIDUES);
    	}
    }

    @FXML
    void onCkbMutAction(ActionEvent event) {
    	if(ckbMutation.isSelected()){
    		tblResults.getColumns().add(clmMUTATION);
    		mSearchFactory.putSearchAble(SearchType.MUTATION);
    	} else {
    		tblResults.getColumns().remove(clmMUTATION);
    		mSearchFactory.removeSearchAble(SearchType.MUTATION);
    	}
    }

    @FXML
    void onCkbResAction(ActionEvent event) {
    	if(ckbResolution.isSelected()){
    		tblResults.getColumns().add(clmRESOLUTION);
    		mSearchFactory.putSearchAble(SearchType.RESOLUTION);
    	} else {
    		tblResults.getColumns().remove(clmRESOLUTION);
    		mSearchFactory.removeSearchAble(SearchType.RESOLUTION);
    	}
    }

    @FXML
    void onCkbEcAction(ActionEvent event) {
    	if(ckbEc.isSelected()){
    		tblResults.getColumns().add(clmEC);
    		mSearchFactory.putSearchAble(SearchType.EC);
    	} else {
    		tblResults.getColumns().remove(clmEC);
    		mSearchFactory.removeSearchAble(SearchType.EC);
    	}
    }

    @FXML
    void onCkbExpdtaAction(ActionEvent event) {
    	if(ckbExpDTA.isSelected()){
    		tblResults.getColumns().add(clmEXPDTA);
    		mSearchFactory.putSearchAble(SearchType.EXPDTA);
    	} else {
    		tblResults.getColumns().remove(clmEXPDTA);
    		mSearchFactory.removeSearchAble(SearchType.EXPDTA);
    	}
    }

    @FXML
    void onCkbChainAction(ActionEvent event) {
    	if(ckbChain.isSelected()){
    		tblResults.getColumns().add(clmCHAIN);
    		mSearchFactory.putSearchAble(SearchType.CHAIN);
    	} else {
    		tblResults.getColumns().remove(clmCHAIN);
    		mSearchFactory.removeSearchAble(SearchType.CHAIN);
    	}
    }

    @FXML
    void onCkbMolAction(ActionEvent event) {
    	if(ckbMolecult.isSelected()){
    		tblResults.getColumns().add(clmMOLECULE);
    		mSearchFactory.putSearchAble(SearchType.MOLECULE);
    	} else {
    		tblResults.getColumns().remove(clmMOLECULE);
    		mSearchFactory.removeSearchAble(SearchType.MOLECULE);
    	}
    }

    @FXML
    void onCkbHetnamAction(ActionEvent event) {
    	
    	if(ckbHetnam.isSelected()){
    		tblResults.getColumns().add(clmHetnam);
    		SearchFactory.getInstance().putSearchAble(SearchType.HETNAM);
    	} else {
    		tblResults.getColumns().remove(clmHetnam);
    		SearchFactory.getInstance().removeSearchAble(SearchType.HETNAM);
    	}
    }
    
    @FXML
    void onClearTableAction(ActionEvent event) {
    	disableKeyWordSelection(false);
    	clearTableBtn.setDisable(true);
    	service.reset();
    	serviceForCustomeSearch.reset();
    	if(selectedIndex == tab1){
    		tblResults.setDisableComponents(true,TableControl.Component.BUTTON_EXPORT);
    	}
    	
    	
    }
    
    protected void disableKeyWordSelection(boolean disable){
    	ckbChain.setDisable(disable);
    	ckbEc.setDisable(disable);
    	ckbExpDTA.setDisable(disable);
    	ckbHetnam.setDisable(disable);
    	ckbMissingRes.setDisable(disable);
    	ckbMolecult.setDisable(disable);
    	ckbMutation.setDisable(disable);
    	ckbResolution.setDisable(disable);
    	tblResults.reloadFirstPage();
    	if(resultData.size() > 0){
    		resultData.clear();
		}
    	searchButtonCustom.setDisable(disable);
    	searchButton.setDisable(disable);
    	
    	if(selectedIndex == tab1){
    		tab2.disableProperty().set(disable);
    	}
    	
    	if(selectedIndex == tab2){
    		tab1.disableProperty().set(disable);
    	}
    }

	
}
