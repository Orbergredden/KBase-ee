package app.view.business;

import app.Main;
import app.lib.AppDataObj;
import app.lib.ConvertType;
import app.lib.DateConv;
import app.lib.ShowAppMsg;
import app.model.AppItem_Interface;
import app.model.ConfigMainList;
import app.model.DBConCur_Parameters;
import app.model.DBConn_Parameters;
import app.model.Params;
import app.model.StateItem;
import app.model.StateList;
import app.model.business.InfoHeaderItem;
import app.model.business.Info_FileItem;
import app.model.business.Info_ImageItem;
import app.model.business.Info_TextItem;
import app.model.business.SectionClipboardInfo;
import app.model.business.SectionItem;
import app.model.business.template.TemplateClipboardInfo;
import app.model.business.template.TemplateItem;
import app.model.business.template.TemplateSimpleItem;
import app.view.structure.TabNavigationHistory;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Контроллер основного фрейма. Показывает дерево разделов (и др.) и инфо по разделам.
 * @author Igor Makarevich
 */
public class SectionList_Controller implements Container_Interface, AppItem_Interface {
	
	private Params params;
    //
    private long rootSectionId;
    
    /**
     * Ковертор даты/времени
     */
    private DateConv dateConv;
    
    @FXML
	private Button button_Exit;
    @FXML
    private TitledPane titledPane_Title;
    
    @FXML
    private SplitPane splitPane_main;
    
    @FXML
    private Tab tab_ContentTree;
    
    @FXML
	public TreeTableView<SectionItem> treeTableView_sections;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_id;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_name;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_iconId;
	@FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_themeId;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_description;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_dateCreated;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_dateModified;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_userCreated;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_userModified;
    @FXML
    private TreeTableColumn<SectionItem, String> treeTableColumn_dateModifiedInfo;
    @FXML
	private TreeTableColumn<SectionItem, String> treeTableColumn_cacheType;

    @FXML
    private MenuItem menuitem_sectionOpen;
    @FXML
    private MenuItem menuitem_sectionOpenInMainTab;
    @FXML
    private MenuItem menuitem_sectionOpenInWindow;
    @FXML
	private MenuItem menuitem_sectionAdd;
    @FXML
	private MenuItem menuitem_sectionUpdate;
    @FXML
	private MenuItem menuitem_sectionDelete;
    @FXML
	private MenuItem menuitem_sectionCopy;
    @FXML
	private MenuItem menuitem_sectionCut;
    @FXML
	private MenuItem menuitem_sectionPaste;
    @FXML
	private MenuItem menuitem_treeRefresh;
    @FXML
	private MenuItem menuitem_treeOpenInMainTab;
    @FXML
	private MenuItem menuitem_treeOpenInWindow;
    
    @FXML
    TabPane tabPane_info;
    @FXML
    private Tab tab_DocCur;

	//
	public SectionList_Controller.TreeView_Controller treeViewCtrl;
    
    /**
     * Контроллер основного документа
     */
    DocumentView_Controller controller_DocView;

    //
	private Preferences prefs;
	
	private TabNavigationHistory tabNavigationHistory;
    
    /**
     * Конструктор.
     * Конструктор вызывается раньше метода initialize().
     */
    public SectionList_Controller() {
    	dateConv = new DateConv();
		treeViewCtrl = this.new TreeView_Controller();
		prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
		tabNavigationHistory = new TabNavigationHistory();
    }
	
    /**
     * Инициализация класса-контроллера. Этот метод вызывается автоматически
     * после того, как fxml-файл будет загружен.
     */
    @FXML
    private void initialize() {        }
    
    /**
     * Вызывается главным приложением, которое даёт параметры.
     * Инициализирует контролы на слое.
     */
    public void setParams(Params params, long rootSectionId) {
    	this.params = params;
        this.rootSectionId = rootSectionId;
        
        // init controls
        initControlsValue();
    }
    
    /**
     * Инициализирует контролы значениями 
     */
    private void initControlsValue() {
    	button_Exit.setTooltip(new Tooltip("Закрыть фрейм"));
    	
    	//======== title
    	titledPane_Title.setText(titledPane_Title.getText() + " - " + params.getConCur().param.getConnName());
    	if (params.getConCur().param.getColorEnable()) {
    		titledPane_Title.setStyle("-fx-body-color: #" + 
	                              ConvertType.colorToHex(new Color(
	                            		  params.getConCur().param.getColorBRed_A(),  params.getConCur().param.getColorBGreen_A(),
	                            		  params.getConCur().param.getColorBBlue_A(), params.getConCur().param.getColorBOpacity_A())) + 
	                              ";" +
	                              "-fx-text-fill: #" +
    			                  ConvertType.colorToHex(new Color(
    			                		  params.getConCur().param.getColorTRed_A(),  params.getConCur().param.getColorTGreen_A(),
    			                		  params.getConCur().param.getColorTBlue_A(), params.getConCur().param.getColorTOpacity_A())) +
                                  ";");
    	}
    	
    	//======== splitPane_main
    	splitPane_main.setDividerPositions(prefs.getDouble("MainInfo_splitPane_main_position", 0.3));
    	
    	splitPane_main.getDividers().get(0).positionProperty().addListener(
                o -> {
                	prefs.putDouble("MainInfo_splitPane_main_position", splitPane_main.getDividerPositions()[0]);
                	//System.out.println(splitPane_main.getDividerPositions()[0]);
                }
        );
    	
    	//======== Content Tabs (left side)
    	// tab_ContentTree
    	// выводим картинку и надпись
    	HBox hbox_ContentTree = new HBox();
    	Label label_TitleContentTree = new Label(" Содержание");
    	hbox_ContentTree.getChildren().add(new ImageView(new Image("file:resources/images/icon_ContentTree_16.png")));
    	hbox_ContentTree.getChildren().add(label_TitleContentTree);
    	tab_ContentTree.setText("");
    	tab_ContentTree.setGraphic(hbox_ContentTree);
    	
    	//======== TreeTableView Sections
		treeViewCtrl.init();

    	// ContextMenu
		menuitem_sectionOpen.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
		menuitem_sectionOpenInMainTab.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
		menuitem_sectionOpenInWindow.setGraphic(new ImageView(new Image("file:resources/images/icon_open_16.png")));
    	menuitem_sectionCopy.setGraphic(new ImageView(new Image("file:resources/images/icon_copy_16.png")));
    	menuitem_sectionCut.setGraphic(new ImageView(new Image("file:resources/images/icon_cut_16.png")));
    	menuitem_sectionPaste.setGraphic(new ImageView(new Image("file:resources/images/icon_paste_16.png")));
    	menuitem_sectionAdd.setGraphic(new ImageView(new Image("file:resources/images/icon_add_16.png")));
    	menuitem_sectionUpdate.setGraphic(new ImageView(new Image("file:resources/images/icon_update_16.png")));
    	menuitem_sectionDelete.setGraphic(new ImageView(new Image("file:resources/images/icon_delete_16.png")));
    	menuitem_treeRefresh.setGraphic(new ImageView(new Image("file:resources/images/icon_refresh_16.png")));
    	menuitem_treeOpenInMainTab.setGraphic(new ImageView(new Image("file:resources/images/icon_open_tree_16.png")));
    	menuitem_treeOpenInWindow.setGraphic(new ImageView(new Image("file:resources/images/icon_open_tree_16.png")));

    	//
    	initTabPaneInfo();
    	
    	//======== init tab_DocCur
    	try {
	    	// Загружаем fxml-файл и создаём новую сцену
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/DocumentView_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Даём контроллеру доступ к главному прилодению (передаем параметры).
			controller_DocView = loader.getController();
			
			Params params = new Params(this.params);
			params.setObjContainer(this);
			params.setParentObj(this);
			
			controller_DocView.setParams(params, false);
			
			// add to tab_DocCur
	    	tab_DocCur.setContent(page);
	    	tab_DocCur.setUserData(controller_DocView);         // запихиваем ссылку на контролер для внешних вызовов
	    	
	    	// add tab icon
	    	HBox hbox_DocCur = new HBox();
	    	Label label_TitleDocCur = new Label(" Текущий");
	    	hbox_DocCur.getChildren().add(new ImageView(new Image("file:resources/images/icon_document_16.png")));
	    	hbox_DocCur.getChildren().add(label_TitleDocCur);
	    	tab_DocCur.setText("");
	    	tab_DocCur.setGraphic(hbox_DocCur);
    	} catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initTabPaneInfo () {
    	//изменение активного таба
    	tabPane_info.getSelectionModel().selectedItemProperty().addListener(
    	    new ChangeListener<Tab>() {
    	        @Override
    	        public void changed(ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) {
    	        	//-------- adding a new tab to the history of active tabs (stack)
    	        	if (newTab != null) {
						AppItem_Interface newTabCtrl = (AppItem_Interface)newTab.getUserData();
    	        	
						if (! tabNavigationHistory.getIsDeleted()) {
							tabNavigationHistory.add(newTabCtrl.getOID());
						}
    	        	}
    	        }
    	    }
    	);
    }
    
    /**
     * Вызывается при выборе раздела в дереве разделов
     */
    private void showSectionInfo(TreeItem<SectionItem> ti) {
    	
    	if (ti != null) {
    		params.setMsgToStatusBar(treeViewCtrl.getSectionPath (ti, 1));
        	
        	//load document
        	controller_DocView.load(ti.getValue().getId(), false);
    	} else {
    		params.setMsgToStatusBar("");
    		
    		//load empty document
        	controller_DocView.loadEmptyPage();
    	}
    }
    
    /**
     * Вызывается при нажатии на кнопке "Закрыть" (X)
     */
    @FXML
    private void handleButtonExit() {
    	
    	//-------- проверка на несохраненные данные
    	if (checkUnsavedData()) {
    		if (ShowAppMsg.showQuestion (
					"CONFIRMATION", 
					"Вопрос", 
					"Во вкладке есть несохраненные данные.", 
					"Сохнять их перед закрытием вкладки ?")) {
				saveAll();
			}
    	}

		//-------- sort
		if (treeTableView_sections.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_sections.getSortOrder().get(0);
			prefs.put("SectionsList_sortColumnId",currentSortColumn.getId());
			prefs.put("SectionsList_sortType",currentSortColumn.getSortType().toString());
		} else {
			prefs.remove("SectionsList_sortColumnId");
			prefs.remove("SectionsList_sortType");
		}

    	//
		params.getObjContainer().closeContainer(getOID());
    }
    
    /**
	 * Открывает раздел (документ) в новой вкладке в табе разделов
	 */
	@FXML
	private void handleButtonOpenSection() {
		
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	//-------- открываем таб для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(this);
		params.setParentObj(this);
		params.setTabPane_Cur(tabPane_info);
		
		AppDataObj.openDocumentView (params, tsi);
	}
	
	/**
	 * Открывает раздел (документ) в новой вкладке в основном табе
	 */
	@FXML
	private void handleButtonOpenSectionInMainTab() {
		
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	//-------- открываем таб для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(params.getRootController());
		params.setParentObj(this);
		params.setTabPane_Cur(params.getTabPane_Main());
		
		AppDataObj.openDocumentView (params, tsi);
		//AppDataObj.openDocumentView (tsi, params.getConCur(), params.getMain(), this, params.getRootController(), params.getTabPane_Main());
	}
	
	/**
	 * Открывает раздел (документ) в новой вкладке в отдельном окне
	 */
	@FXML
	private void handleButtonOpenSectionInWindow() {
		
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();

		//-------- открываем окно для просмотра документа
		Params params = new Params(this.params);
		params.setObjContainer(params.getWinList());
		params.setParentObj(this);
		params.setTabPane_Cur(null);
		
		(new AppDataObj()).openDocumentViewInWin(params, tsi);
	}
    
    /**
	 * Добавляем новый раздел
	 */
	@FXML
	private void handleButtonAddSection() {
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел", 
					"Выберите раздел, в который добавиться новый подраздел.");
			return;
		}
		
		try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionEdit_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Добавление нового раздела");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Sections_24.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
	    	dialogStage.setWidth(prefs.getDouble("stageSectionsEdit_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageSectionsEdit_Height", 700));
			dialogStage.setX(prefs.getDouble("stageSectionsEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageSectionsEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			SectionEdit_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
	        controller.setParams(params, 1, treeTableView_sections.getSelectionModel().getSelectedItem());
			
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
	        
	        //
	        ///////////treeTableView_sections.refresh();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Изменяем текущий раздел в справочнике
	 */
	@FXML
	private void handleButtonUpdateSection() {
		if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
			ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел", "Выберите раздел для редактирования");
			return;
		}
		
		try {
	    	// Загружаем fxml-файл и создаём новую сцену
			// для всплывающего диалогового окна.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("view/business/SectionEdit_Layout.fxml"));
			AnchorPane page = loader.load();
		
			// Создаём диалоговое окно Stage.
			Stage dialogStage = new Stage();
			dialogStage.setTitle("Редактирование раздела");
			dialogStage.initModality(Modality.NONE);
			dialogStage.initOwner(params.getMainStage());
			Scene scene = new Scene(page);
			dialogStage.setScene(scene);
			dialogStage.getIcons().add(new Image("file:resources/images/icon_Sections_24.png"));
			
			Preferences prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
			dialogStage.setWidth(prefs.getDouble("stageSectionsEdit_Width", 500));
			dialogStage.setHeight(prefs.getDouble("stageSectionsEdit_Height", 700));
			dialogStage.setX(prefs.getDouble("stageSectionsEdit_PosX", 0));
			dialogStage.setY(prefs.getDouble("stageSectionsEdit_PosY", 0));
			
			// Даём контроллеру доступ к главному прилодению.
			SectionEdit_Controller controller = loader.getController();
			
			Params params = new Params(this.params);
			params.setParentObj(this);
			params.setStageCur(dialogStage);
			
	        controller.setParams(params, 2, treeTableView_sections.getSelectionModel().getSelectedItem());
	        
	        // Отображаем диалоговое окно и ждём, пока пользователь его не закроет
	        dialogStage.showAndWait();
		} catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Удаляем текущий раздел
	 */
	@FXML
	private void handleButtonDeleteSection() {
		TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
		
		if (selectedItem == null) {
			params.setMsgToStatusBar("Ничего не выбрано для удаления.");
			return;
		}
		
		SectionItem sip = selectedItem.getValue();
		
		if (sip.getId() == 0) {
			ShowAppMsg.showAlert("INFORMATION", "Удаление раздела", 
					"Удаление раздела '"+ sip.getName() +"'", "Удалять корневой раздел нельзя !");
			return;
		}
		
		if (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление раздела", 
	            "Удаление раздела '"+ sip.getName() +"'", "Удалить раздел ?"))
			return;
		
		int childrenCount = params.getConCur().db.sectionGetNumberOfChildren(sip.getId());
		
		if ((childrenCount > 0) && (! ShowAppMsg.showQuestion("CONFIRMATION", "Удаление раздела", 
	            "Раздел '"+ sip.getName() +"' содержит подраздел(ы).", "Удалить раздел вместе с подразделами ?")))  
			return;
		
		// delete from DB 
		params.getConCur().db.sectionDelete(sip.getId());
		
		// delete from TreeTableView
		TreeItem<SectionItem> parentItem = selectedItem.getParent();
		if (parentItem != null) {     // текущая иконка не корневая
	        parentItem.getChildren().remove(selectedItem);
		}
		
		// выводим сообщение в статус бар
		params.setMsgToStatusBar("Раздел '" + sip.getName() + "' удален.");
	}

	/**
     * Копирует текущий раздел в локальный буфер обмена
     */
    @FXML
    private void handleButtonSectionCopy() {
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	saveToClipboard (SectionClipboardInfo.TYPE_OPER_COPY);
    	
        params.setMsgToStatusBar("Раздел '"+ selectedItem.getValue().getName() +"' скопирован в локальный буфер обмена.");
    }
    
    /**
     * Вырезает текущий раздел с занесением в локальный буфер обмена
     */
    @FXML
    private void handleButtonSectionCut() {
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	saveToClipboard (SectionClipboardInfo.TYPE_OPER_CUT);
    	
        params.setMsgToStatusBar("Раздел '"+ selectedItem.getValue().getName() +"' вырезан в локальный буфер обмена.");
    }
    
    /**
     * Вставляет раздел указанный в буфере обмена
     */
    @FXML
    private void handleButtonSectionPaste() {
    	SectionClipboardInfo clip;
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	SectionItem item;
    	
    	String path = params.getConfig().getItemValue("directories", "PathDirCache") + "clipboard/";
    	
    	//-------- check 
    	if (selectedItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран элемент", 
    				"Выберите элемент.");
    		return;
    	}
    	
    	item = selectedItem.getValue();
    	
    	//--------- read info object
    	clip = SectionClipboardInfo.unserialize(path, SectionClipboardInfo.FILE_NAME_INFO);
    	
    	// check CUT for different connections
    	if ((clip.getTypeOper() == TemplateClipboardInfo.TYPE_OPER_CUT) && 
    		(clip.getDbCon_Id() != params.getConCur().Id)) {
    		ShowAppMsg.showAlert("WARNING", "Перенос об'екту", 
    				"Неможливо переносити об'єкт між різними БД, тільки копіювати.", "");
    		return;
    	}
    	
    	// read section object
    	SectionItem sectionItem = 
    			SectionItem.unserialize(path, SectionClipboardInfo.FILE_NAME, SectionClipboardInfo.FILE_NAME_ICON);
    	
    	// copy or cut object
    	switch (clip.getTypeOper()) {
    	case SectionClipboardInfo.TYPE_OPER_COPY :
    		if (clip.getDbCon_Id() == params.getConCur().Id) {  // вставляємо в туж саму БД (взяв старий код)
    			boolean copyWithSubSection = prefs.get("copyWithSubSection", "No").equals("Yes");
            	int retVal = ShowAppMsg.showQuestionWithOption(
            			"CONFIRMATION", "Копирование раздела", 
                          "Копировать раздел '"+ sectionItem.getName() +"' ?", null,
                          "Копировать ветку целиком", copyWithSubSection);
            	long newSectionId = 0;
            	
            	if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только один раздел
            		newSectionId = treeViewCtrl.copySection (sectionItem, selectedItem, false);
            		
            		// save Option value
            		prefs.put("copyWithSubSection", "No");
            		
            		// выводим сообщение в статус бар
            		params.setMsgToStatusBar("Раздел '" + sectionItem.getName() + "' скопирован.");
            	}
            	if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
            		newSectionId = treeViewCtrl.copySection (sectionItem, selectedItem, true);
            		
            		// save Option value
            		prefs.put("copyWithSubSection", "Yes");
            		
            		// выводим сообщение в статус бар
            		params.setMsgToStatusBar("Раздел (ветка) '" + sectionItem.getName() + "' скопирована.");
            	}
            	
            	treeTableView_sections.sort();
            	treeViewCtrl.expandTreeItemsById(treeViewCtrl.root, newSectionId);
            	treeViewCtrl.selectTreeItemById(treeViewCtrl.root, newSectionId);
    		} else {      // бази даних різні
    			//---- десеріалізація та підготовка даних, додаємо в БД
    			sectionItem.setId(params.getConCur().db.sectionNextId());
    			sectionItem.setParentId(item.getId());
    			sectionItem.setIconId(0);
    			sectionItem.setIconIdRoot(0);
    			sectionItem.setIconIdDef(0);
    			sectionItem.setThemeId(item.getThemeId());
    			
    			params.getConCur().db.sectionAdd(sectionItem);
    			
    			//
    			InfoHeaderItem ihi;
    			
    			for (int i=1; i<=clip.getNumberOfInfoBlocks(); i++) {
    				ihi = InfoHeaderItem.unserialize(path, 
    						SectionClipboardInfo.FILE_PREFIX_INFO_HEADER+i+SectionClipboardInfo.FILE_POSTFIX);
    				ihi.setId(params.getConCur().db.infoNextId());
    				ihi.setSectionId(sectionItem.getId());
    				ihi.setTemplateStyleId(0);
    				//infoId ...
    				
    				switch ((int)ihi.getInfoTypeId()) {
    	    		case 1 :    // Простий текст
    	    			Info_TextItem it = Info_TextItem.unserialize(path, 
    	    					SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+i+SectionClipboardInfo.FILE_POSTFIX);
    	    			it.setId(params.getConCur().db.info_TextNextId());
    	    			ihi.setInfoId(it.getId());
    	    			params.getConCur().db.info_TextAdd(it);
    	    			break;
    	    		case 2 :    // Зображення
    	    			Info_ImageItem ii = Info_ImageItem.unserialize(path, 
    	    					SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+i+SectionClipboardInfo.FILE_POSTFIX,
    	        				SectionClipboardInfo.FILE_PREFIX_INFO_FILE +i+SectionClipboardInfo.FILE_POSTFIX);
    	    			ii.setId(params.getConCur().db.info_ImageNextId());
    	    			ihi.setInfoId(ii.getId());
    	    			params.getConCur().db.info_ImageAdd(ii, 
    	    					path+SectionClipboardInfo.FILE_PREFIX_INFO_FILE +i+SectionClipboardInfo.FILE_POSTFIX);
    	    			break;
    	    		case 3 :    // Файл
    	    			Info_FileItem ifl = Info_FileItem.unserialize(path, 
    	    					SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+i+SectionClipboardInfo.FILE_POSTFIX);
    	    			ifl.setId(params.getConCur().db.info_TextNextId());
    	    			ifl.setIconId(0);;
    	    			ihi.setInfoId(ifl.getId());
    	    			params.getConCur().db.info_FileAdd(ifl, null);
    	    			break;
    	    		default :
    	    			ShowAppMsg.showAlert("WARNING", "Copy/Cut to local clipboard", 
    	    					"Невідомий тип інфоблока", 
    	   		                "Тип інфоблока "+ihi.getInfoTypeId()+" не визначений");
    	    		}
    				
    				params.getConCur().db.infoAdd(ihi);
    			}
    			
    			//---- додаємо в дерево-контрол
    			TreeItem<SectionItem> subItemS = new TreeItem<>(sectionItem);
    			selectedItem.getChildren().add(subItemS);
    			selectedItem.setExpanded(true);
    		}
    		break;
    	case SectionClipboardInfo.TYPE_OPER_CUT :
    		TreeItem<SectionItem> tiSectionFromSer = new TreeItem<SectionItem> (sectionItem); 
    		
    		selectedItem.getChildren().add(tiSectionFromSer);
    		treeTableView_sections.sort();
    		treeTableView_sections.getSelectionModel().select(tiSectionFromSer);
			//treeViewCtrl.selectTreeItemById(treeTableView_sections.getRoot(), clipBoard_tiSection.getValue().getId());
    		
    		// update in DB
    		params.getConCur().db.sectionMove (sectionItem.getId(), item.getId());
    		
    		// выводим сообщение в статус бар
    		params.setMsgToStatusBar("Раздел '" + sectionItem.getName() + "' перемещен.");
    		
    		break;
    	}
    }
    
    /**
     * Обновляет ветку разделов
     */
    @FXML
    private void handleButtonRefreshTree() {
    	StateList stateListTree = new StateList();
    	TreeItem<SectionItem> mainItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	if (mainItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо обновить.");
    		return;
    	}
    	
    	//==== save state
    	try {
    		stateListTree.add(
					"TreeItemSelected",
					Long.toString(mainItem.getValue().getId()),      // section id in DB,
					null);
		} catch (NullPointerException ex) {    }
    	
   		addTreeItemStateRecursive(stateListTree, mainItem);
    	
    	stateListTree.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);
    	
    	//==== удаляем ветку (без текущего итема, только дочерние)
    	mainItem.getChildren().clear();
    	
    	//==== обновляем текущий итем, если нужно
    	if (mainItem.getValue().getId() != 0) {
    		mainItem.setValue(params.getConCur().db.sectionGetById(mainItem.getValue().getId()));
    	}
    	
    	//==== создаем ветку заново
    	treeViewCtrl.initTreeItemsRecursive(mainItem);

    	//==== восстанавливаем состояние ветки
    	ObservableList<Long> listItemsForExpand = FXCollections.observableArrayList();
		Long selectedItemId = new Long(0);
    	
    	for (StateItem si : stateListTree.list) {
			switch (si.getName()) {
				case "TreeItemSelected" :
					selectedItemId = new Long(si.getParams());
					break;
				case "TreeItemExpanded":
					listItemsForExpand.add(new Long(si.getParams()));
					break;
				case "TreeItemsDoExpandAndSelected" :
					restoreTreeItemStateRecursive(listItemsForExpand,mainItem);
					treeTableView_sections.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,mainItem);
					treeTableView_sections.sort();
					break;
			}
    	}	
    	
    	//==== рефрешнуть документ
    	showSectionInfo(mainItem);
    }
    
    /**
     * Открывает ветку разделов в новой вкладке в основном табе
     */
    @FXML
    private void handleButtonOpenTreeInMainTab() {
    	
    	if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
    	//-------- открываем таб для ветки разделов
		Params params = new Params(this.params);
		params.setObjContainer(params.getRootController());
		params.setTabPane_Cur(params.getTabPane_Main());
		
		AppDataObj.openSectionTree (params, tsi.getValue().getId());
    }
    
    /**
     * Открывает ветку разделов в новом окне
     */
    @FXML
    private void handleButtonOpenTreeInWindow() {
    	
    	if (treeTableView_sections.getSelectionModel().getSelectedItem() == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран раздел в списке", 
    				"Выберите раздел, который необходимо открыть.");
    		return;
    	}

		TreeItem<SectionItem> tsi = treeTableView_sections.getSelectionModel().getSelectedItem();
    	
		//-------- открываем окно
		Params params = new Params(this.params);
		params.setObjContainer(params.getWinList());
		params.setTabPane_Cur(null);
		
		(new AppDataObj()).openSectionTreeInWin(params, tsi.getValue().getId());
    }
    
    /**
	 * Реализуем метод интерфейса Container_Interface.
     * Показывает состояние инфо блока во внешнем контейнере - были несохраненные изменения или нет.
	 */
	public void showStateChanged(int oid, boolean isChanged) {
        Tab ourTab;
        HBox hbox;
        String changingIconId = "changingIconId";

        //---- ищем таб по ИД
        for (int i=0; i<tabPane_info.getTabs().size(); i++) {
        	if ((tabPane_info.getTabs().get(i).getUserData() != null) && 
    			(((AppItem_Interface)tabPane_info.getTabs().get(i).getUserData()).getOID() == oid)) {
                ourTab = tabPane_info.getTabs().get(i);
                hbox = (HBox) ourTab.getGraphic();

                if (isChanged) {               // was change
                    if ((hbox.getChildren().get(1).getId() == null) ||
                        (! hbox.getChildren().get(1).getId().equals(changingIconId))) {
                        ImageView imageView_changed;

                        imageView_changed = new ImageView(new Image("file:resources/images/icon_edited_16.png"));
                        imageView_changed.setId(changingIconId);

						hbox.getChildren().add(1, imageView_changed);
                    }
                } else {                       // no change
                    if ((hbox.getChildren().get(1).getId() != null) &&
                        (hbox.getChildren().get(1).getId().equals(changingIconId))) {
                        hbox.getChildren().remove(1);
                    }
                }
                
                // меняем состояние у родителя
                if (isChanged) {
                	params.getObjContainer().showStateChanged(getOID(), isChanged);
                } else {
                	if (! checkUnsavedData()) {
                		params.getObjContainer().showStateChanged(getOID(), isChanged);
                	}
                }
            }
        }
	}

    /**
     * Реализуем метод интерфейса Container_Interface.
     * Закрываем фрейм с редактированием инфо блока
     */
	public void closeContainer (int oid) {
        for (int i=0; i<tabPane_info.getTabs().size(); i++) {
			if ((tabPane_info.getTabs().get(i).getUserData() != null) && 
				(((AppItem_Interface)tabPane_info.getTabs().get(i).getUserData()).getOID() == oid)) {
				
				tabNavigationHistory.delete(oid);
				
				tabPane_info.getTabs().remove(i);
			}
		}
		
		tabNavigationHistory.setIsDeleted(false);
		int lastOId = tabNavigationHistory.getLast();
		
		if (lastOId != 0) {
			for (int i=0; i<tabPane_info.getTabs().size(); i++) {
				if ((tabPane_info.getTabs().get(i).getUserData() != null) && 
					(((AppItem_Interface)tabPane_info.getTabs().get(i).getUserData()).getOID() == lastOId)) {
					
					// activate tab
					tabPane_info.getSelectionModel().select(i);
				}
			}
		}
    }
	
	/**
	 * уникальный ИД обьекта
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getOID() {
		return hashCode();
	}

	/**
	 * Название элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public String getName() {
		return AppItem_Interface.ELEMENT_SECTION_LIST;
	}

	/**
	 * id соединения с базой данных
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public int getDbConnId() {
		return params.getConCur().Id;
	}

	/**
	 * контроллер элемента приложения
	 * Реализуем метод интерфейса AppItem_Interface.
	 */
	public Object getController() {
		return this;
	}
	
	/**
     * Корень дерева
     * Реализуем метод интерфейса AppItem_Interface.
     */
    public long getRootId() {
    	return rootSectionId;
    }
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.            <br>
	 * Проверяем наличие несохраненных данных
	 */
	public boolean checkUnsavedData () {
		AppItem_Interface appItem;
		
		for (int i=1; i<tabPane_info.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_info.getTabs().get(i).getUserData();
			if (appItem.checkUnsavedData()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Реализуем метод интерфейса AppItem_Interface.
     * Сохранение  измененной информации в БД
     */
    public void save () {
    	int curentIndex = tabPane_info.getSelectionModel().getSelectedIndex();
    	
    	if (curentIndex > 0) {
    		AppItem_Interface appItem = (AppItem_Interface)tabPane_info.getTabs().get(curentIndex).getUserData();
    		appItem.save();
    	}
    }
    
    /**
	 * Реализуем метод интерфейса AppItem_Interface.
     * Сохраняем всю измененную информацию в БД
     */
    public void saveAll () {
    	AppItem_Interface appItem;

		for (int i=1; i<tabPane_info.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_info.getTabs().get(i).getUserData();
			appItem.saveAll();
		}
    }

    /**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Сохраняем состояние контролов в иерархической структуре
	 */
	public void saveControlsState (StateList stateList) {

		//-------- treeTableView_sections
		String sortColumnId;
		String sortType;
		
		if (treeTableView_sections.getSortOrder().size() > 0) {     // при сортировке по нескольким столбцам поменять if на for
			TreeTableColumn currentSortColumn = (TreeTableColumn) treeTableView_sections.getSortOrder().get(0);
			
			sortColumnId = currentSortColumn.getId();
			sortType = currentSortColumn.getSortType().toString();
		} else {
			sortColumnId = "";
			sortType = "";
		}
		stateList.add("TreeTable_sortColumnId",	sortColumnId, null);
		stateList.add("TreeTable_sortType",	sortType, null);
		stateList.add("TreeTable_doSort", "", null);
		
		try {
			stateList.add(
					"TreeItemSelected",
					Long.toString(treeTableView_sections.getSelectionModel().getSelectedItem().getValue().getId()),      // section id in DB,
					null);
		} catch (NullPointerException ex) {    }
		addTreeItemStateRecursive(stateList,treeTableView_sections.getRoot());
		stateList.add(
				"TreeItemsDoExpandAndSelected",
				"",
				null);
		
		//-------- save main split state
		stateList.add(
				"splitPane_main_Position",
				//Double.toString(splitPane_info.getDividerPositions()[0]),
				String.valueOf(splitPane_main.getDividerPositions()[0]),
				null);

		//--------- save tabs state
		AppItem_Interface appItem;
		DBConCur_Parameters conCur;           // обьект текущего соединения
		DBConn_Parameters conPar;             // параметры текущего соединения
		StateItem stateItem;
		
		// "Текущий" таб
		appItem = (AppItem_Interface)tabPane_info.getTabs().get(0).getUserData();
		stateItem = stateList.add(
				"tabSubItems",
				"",
				new StateList());
		appItem.saveControlsState(stateItem.subItems);

		for (int i=1; i<tabPane_info.getTabs().size(); i++) {
			appItem = (AppItem_Interface)tabPane_info.getTabs().get(i).getUserData();
			conCur = params.getConnDB().conList.get(params.getConnDB().getIndexById(appItem.getDbConnId()));
			conPar = conCur.param;
			
			Tab curTab = tabPane_info.getTabs().get(i);
			HBox hbox = (HBox) curTab.getGraphic();
			Node nodeTitle;
			if (hbox.getChildren().size() == 3) {
				nodeTitle = hbox.getChildren().get(2);
			} else {
				nodeTitle = hbox.getChildren().get(1);
			}
			String tabTitle = ((Label)nodeTitle).getText();

			stateList.add(
					"tabName",
					appItem.getName(),
					null);
			stateList.add(
					"tabAppItemId",
					Long.toString(appItem.getAppItemId()),
					null);
			stateList.add(
					"tabDbConnId",
					Integer.toString(conPar.getConnId()),
					null);
			stateList.add(
					"tabCreateAction",
					"",
					null);
			stateList.add(
					"tabRenameTitleAction",
					tabTitle,
					null);
			stateItem = stateList.add(
					"tabSubItems",
					"",
					new StateList());
			appItem.saveControlsState(stateItem.subItems);
		}

		// active tab
		stateList.add(
				"tabActiveIndex",
				Integer.toString(tabPane_info.getSelectionModel().getSelectedIndex()),
				null);
	}

	/**
	 * рекурсивное сохранение развернутых разделов из дерева разделов
	 */
	private void addTreeItemStateRecursive(StateList stateList, TreeItem<SectionItem> ti) {

		//-------- проверяем и записываем развернутость итема
		if (ti.isExpanded()) {
			stateList.add(
					"TreeItemExpanded",
					Long.toString(ti.getValue().getId()),      // section id in DB
					null);
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionItem> i : ti.getChildren()) {
			addTreeItemStateRecursive(stateList, i);
		}
	}

	/**
	 * Реализуем метод интерфейса AppItem_Interface.
	 * Восстанавливаем состояние контролов из иерархической структуры
	 */
	public void restoreControlsState (StateList stateList) {
		// for TreeColumn sort
		String sortColumnId = "";
		String sortType = "";
		// for TreeItems
		ObservableList<Long> listItemsForExpand = FXCollections.observableArrayList();
		Long selectedItemId = 0L;
		// for tabs
		String tabName = null;
		long tabAppItemId = 0;
		int tabDbConnId = 0;

		for (StateItem si : stateList.list) {
			switch (si.getName()) {
				//======== TreeTable sort column
				case "TreeTable_sortColumnId" :
					sortColumnId = si.getParams();
					break;
				case "TreeTable_sortType" :
					sortType = si.getParams();
					break;
				case "TreeTable_doSort" :
					treeTableView_sections.getSortOrder().clear();
					
					if (! sortColumnId.equals("")) {
						for (TreeTableColumn column : treeTableView_sections.getColumns()) {
							if (column.getId().equals(sortColumnId)) {
								treeTableView_sections.setSortMode(TreeSortMode.ALL_DESCENDANTS);
								column.setSortable(true); // This performs a sort
								treeTableView_sections.getSortOrder().add(column);
								if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
								else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
								treeTableView_sections.sort();
							}
						}
					}
					break;
			
				//======== TreeItems state
				case "TreeItemSelected" :
					selectedItemId = new Long(si.getParams());
					break;
				case "TreeItemExpanded":
					listItemsForExpand.add(new Long(si.getParams()));
					break;
				case "TreeItemsDoExpandAndSelected" :
					restoreTreeItemStateRecursive(listItemsForExpand,treeTableView_sections.getRoot());
					treeTableView_sections.sort();
					restoreTreeItemSelectedRecursive(selectedItemId,treeTableView_sections.getRoot());
					treeTableView_sections.sort();
					break;
				//======== restore main split state
				case "splitPane_main_Position" :
					splitPane_main.setDividerPositions(Double.parseDouble(si.getParams()));  
					break;
				//======== tabs state
				case "tabName" :
					tabName = si.getParams();
					break;
				case "tabAppItemId" :
					tabAppItemId = Long.parseLong(si.getParams());
					break;
				case "tabDbConnId" :
					tabDbConnId = Integer.parseInt(si.getParams());
					break;
				case "tabCreateAction" :
					switch (tabName) {
						case AppItem_Interface.ELEMENT_DOCUMENT_VIEW :
							Params par = new Params(this.params);
							par.setObjContainer(this);
							par.setParentObj(this);
							par.setTabPane_Cur(tabPane_info);
							par.setStageCur(par.getMainStage());
							
							AppDataObj.openDocumentView (
									par,
									treeViewCtrl.getTreeItemById(treeViewCtrl.root, tabAppItemId));
							break;
						case AppItem_Interface.ELEMENT_INFO_EDIT :
							Params parIE = new Params(this.params);
							parIE.setObjContainer(this);
							parIE.setParentObj(this);
							parIE.setTabPane_Cur(tabPane_info);
							parIE.setStageCur(parIE.getMainStage());
							
							AppDataObj.openEditInfo (parIE, parIE.getConCur().db.infoGet(tabAppItemId));
							
							break;
					}
					break;
				case "tabRenameTitleAction" :
					// берем последний созданный таб и изменяем в нем заголовок 
					if (tabPane_info.getTabs().size() > 1) {
						String tabTitle = si.getParams();
						int tabIndex = tabPane_info.getTabs().size()-1;
						Tab curTab = tabPane_info.getTabs().get(tabIndex);
						HBox hbox = (HBox) curTab.getGraphic();
						Node nodeTitle = hbox.getChildren().get(1);
						
						((Label)nodeTitle).setText(tabTitle);
					}
					break;
				case "tabSubItems" :
					// берем последний созданный таб и вызываем в нем метод восстановления состояния
					//if (tabPane_info.getTabs().size() > 1) {
						int tabIndex = tabPane_info.getTabs().size()-1;
						AppItem_Interface ai =
								(AppItem_Interface)tabPane_info.getTabs().get(tabIndex).getUserData();
						ai.restoreControlsState(si.subItems);
					//}
					break;	
				case "tabActiveIndex" :
					tabPane_info.getSelectionModel().select(Integer.parseInt(si.getParams()));
					break;
			}
		}
	}

	/**
	 * рекурсивное восстановление состояния разделов дерева разделов
	 */
	private void restoreTreeItemStateRecursive(
			ObservableList<Long> listItemsForExpand,
			TreeItem<SectionItem> ti) {

		//-------- проверяем и разворачиваем текущий итем
		for (Long i : listItemsForExpand) {
			if (i == ti.getValue().getId()) {
				ti.setExpanded(true);
				break;
			}
		}
		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionItem> i : ti.getChildren()) {
			restoreTreeItemStateRecursive(listItemsForExpand, i);
		}
	}

	/**
	 * рекурсивно ищем активный раздел в дереве разделов и выбираем его
	 */
	private void restoreTreeItemSelectedRecursive(
			Long selectedItemId,
			TreeItem<SectionItem> ti) {

		//---------- проверяем и выбираем текущий итем
		if (selectedItemId == ti.getValue().getId())
			treeTableView_sections.getSelectionModel().select(ti);

		//-------- выбираем дочерние итемы и запускаем рекурсию
		for (TreeItem<SectionItem> i : ti.getChildren()) {
			restoreTreeItemSelectedRecursive(selectedItemId, i);
		}
	}
	
	/**
     * Зберігає поточний документ з інфоблоками у буфері обміну
     * @param typeOper
     */
    private void saveToClipboard(int typeOper) {
    	TreeItem<SectionItem> selectedItem = treeTableView_sections.getSelectionModel().getSelectedItem();
    	SectionItem item;
    	SectionClipboardInfo clip;
    	
    	ConfigMainList config = new ConfigMainList();
    	String path = config.getItemValue("directories", "PathDirCache") + "clipboard/";
    	File fDocDir = new File(path);
    	
    	//-------- check 
    	if (selectedItem == null) {
    		ShowAppMsg.showAlert("WARNING", "Нет выбора", "Не выбран элемент", 
    				"Выберите элемент.");
    		return;
    	}
    	
    	item = selectedItem.getValue();
    	
    	if (item.getId() == 0) {
    		ShowAppMsg.showAlert("WARNING", "Не копіюється", 
    				"\"" + item.getName() + "\"", 
    				"Вибраний тип елементів не копіюється в локальний буфер обміну.");
    		return;
    	}
    	
    	//-------- init and save
    	// create dir
    	if (! fDocDir.exists()) {
			if (! fDocDir.mkdirs()) {
				ShowAppMsg.showAlert("WARNING", "Створення директорії", 
        				"\"" + path + "\"", 
        				"Неможливо створити директорію.");
        		return;
			}
    	}
    	
    	// init info object and write
    	clip = new SectionClipboardInfo (
				typeOper, 
				params.getConCur().Id,
				params.getConCur().param.getConnName(),
				params.getConCur().db.sectionGetNumberOfInfoBlocks(item.getId())
				);
    	clip.serialize(path, SectionClipboardInfo.FILE_NAME_INFO);
    	
    	// write section
    	item.serialize(path, SectionClipboardInfo.FILE_NAME, SectionClipboardInfo.FILE_NAME_ICON);
    	
    	// write infoblocks
    	List<InfoHeaderItem> listInfoHeaders = params.getConCur().db.infoListBySectionId(item.getId());
    	int cnt = 0;
    	
    	for (InfoHeaderItem i : listInfoHeaders) {
			cnt++;
    		i.serialize(path, 
    				SectionClipboardInfo.FILE_PREFIX_INFO_HEADER+cnt+SectionClipboardInfo.FILE_POSTFIX);
    		
    		switch ((int)i.getInfoTypeId()) {
    		case 1 :    // Простий текст
    			Info_TextItem it = params.getConCur().db.info_TextGet(i.getInfoId());
    			it.serialize(path, 
        				SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+cnt+SectionClipboardInfo.FILE_POSTFIX);
    			break;
    		case 2 :    // Зображення
    			Info_ImageItem ii = params.getConCur().db.info_ImageGet(i.getInfoId());
    			ii.serialize(path, 
        				SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+cnt+SectionClipboardInfo.FILE_POSTFIX,
        				SectionClipboardInfo.FILE_PREFIX_INFO_FILE +cnt+SectionClipboardInfo.FILE_POSTFIX);
    			break;
    		case 3 :    // Файл
    			Info_FileItem ifl = params.getConCur().db.info_FileGet(i.getInfoId());
    			ifl.serialize(path, 
        				SectionClipboardInfo.FILE_PREFIX_INFO_BLOCK+cnt+SectionClipboardInfo.FILE_POSTFIX);
    			break;
    		default :
    			ShowAppMsg.showAlert("WARNING", "Copy/Cut to local clipboard", 
    					"Невідомий тип інфоблока", 
   		                "Тип інфоблока "+i.getInfoTypeId()+" не визначений");
    		}
		}
    }

	/**
	 * Класс обработки дерева разделов
	 */
	public class TreeView_Controller {
		public TreeItem<SectionItem> root;

		/**
		 * инициализируем дерево, основной метод инициализации
		 */
		void init () {
			initCellValueFactory ();
			initColumnWidth ();

			initRoot ();
			initTreeItemsRecursive(root);

			initCellFactory();
			initRowFactory();

			// Слушаем изменения выбора, и при изменении отображаем информацию .
			treeTableView_sections.getSelectionModel().selectedItemProperty().addListener(
					(observable, oldValue, newValue) -> showSectionInfo(newValue));

			initSortColumn();
		}
		
		/**
		 * Создаем корневой раздел в дереве-контроле
		 */
		private void initRoot () {
			
			if (rootSectionId == 0) {
				root = new TreeItem<>(new SectionItem(
						0, 0, "Всё",
						0, new Image("file:resources/images/icon_Sections_24.png"),
						"это корень, он не редактируется",
						null, null, "", "", null, 0,
						Long.parseLong(params.getConCur().db.settingsGetValue("SECTION_ICON_DEFAULT")),
						Long.parseLong(params.getConCur().db.settingsGetValue("SECTION_THEME_DEFAULT")),
						0));
			} else {
				root = new TreeItem<>(params.getConCur().db.sectionGetById(rootSectionId));
			}
			
			root.setExpanded(true);
			treeTableView_sections.setShowRoot(true);
			treeTableView_sections.setRoot(root);
		}

		/**
		 * initCellValueFactory ()
		 */
		private void initCellValueFactory () {
			treeTableColumn_id.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(Long.toString(param.getValue().getValue().getId()))
			);
			treeTableColumn_name.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getName())
			);
			treeTableColumn_iconId.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									(param.getValue().getValue().getIconId() == 0) ?
											"" : Long.toString(param.getValue().getValue().getIconId())
							));
			treeTableColumn_themeId.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									(param.getValue().getValue().getThemeId() == 0) ?
											"" : Long.toString(param.getValue().getValue().getThemeId())
							));
			treeTableColumn_description.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getDescr())
			);
			treeTableColumn_dateCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateCreated()))
			);
			treeTableColumn_dateModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModified()))
			);
			treeTableColumn_userCreated.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserCreated())
			);
			treeTableColumn_userModified.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(param.getValue().getValue().getUserModified())
			);
			treeTableColumn_dateModifiedInfo.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(dateConv.dateTimeToStr(param.getValue().getValue().getDateModifiedInfo()))
			);
			treeTableColumn_cacheType.setCellValueFactory(
					(TreeTableColumn.CellDataFeatures<SectionItem, String> param) ->
							new ReadOnlyStringWrapper(
									(param.getValue().getValue().getCacheType() == 0) ?
											"" : Integer.toString(param.getValue().getValue().getCacheType())
							));
		}

		/**
		 * initColumnWidth ()
		 */
		private void initColumnWidth () {
			// set/get Pref Width
			treeTableColumn_id.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_id__PrefWidth", 50));

			treeTableColumn_id.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_id__PrefWidth", t1.doubleValue());
					//System.out.print(treeTableColumn_name.getText() + "  ");
					//System.out.println(t1);
				}
			});

			treeTableColumn_name.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_name__PrefWidth", 250));

			treeTableColumn_name.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_name__PrefWidth", t1.doubleValue());
					//System.out.print(treeTableColumn_name.getText() + "  ");
					//System.out.println(t1);
				}
			});

			treeTableColumn_iconId.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_iconId__PrefWidth", 50));

			treeTableColumn_iconId.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_iconId__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_themeId.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_themeId__PrefWidth", 50));

			treeTableColumn_themeId.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_themeId__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_description.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_description__PrefWidth", 250));

			treeTableColumn_description.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_description__PrefWidth", t1.doubleValue());
					//System.out.print(treeTableColumn_name.getText() + "  ");
					//System.out.println(t1);
				}
			});

			treeTableColumn_dateCreated.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_dateCreated__PrefWidth", 150));

			treeTableColumn_dateCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_dateCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateModified.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_dateModified__PrefWidth", 150));

			treeTableColumn_dateModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_dateModified__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userCreated.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_userCreated__PrefWidth", 150));

			treeTableColumn_userCreated.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_userCreated__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_userModified.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_userModified__PrefWidth", 150));

			treeTableColumn_userModified.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_userModified__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_dateModifiedInfo.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_dateModifiedInfo__PrefWidth", 150));

			treeTableColumn_dateModifiedInfo.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_dateModifiedInfo__PrefWidth", t1.doubleValue());
				}
			});

			treeTableColumn_cacheType.setPrefWidth(prefs.getDouble("MainInfo__treeTableColumn_cacheType__PrefWidth", 50));

			treeTableColumn_cacheType.widthProperty().addListener(new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> ov, Number t, Number t1) {
					prefs.putDouble("MainInfo__treeTableColumn_cacheType__PrefWidth", t1.doubleValue());
				}
			});
		}

		/**
		 * Инициализация TreeTableView. Рекурсия по дереву.
		 */
		private void initTreeItemsRecursive(TreeItem<SectionItem> ti) {
			SectionItem f = ti.getValue();

			if (f != null) {
				List<SectionItem> sectionsList = params.getConCur().db.sectionListByParentId (f.getId());

				for (SectionItem i : sectionsList) {
					TreeItem<SectionItem> subItem = new TreeItem<>(i);
					ti.getChildren().add(subItem);

					initTreeItemsRecursive(subItem);
				}
			}
		}

		/**
		 * CellFactory - показ иконок
		 */
		public void initCellFactory () {
			treeTableColumn_name.setCellFactory(ttc -> new TreeTableCell<SectionItem, String>() {
				private SectionItem row;
				private ImageView graphic;

				@Override
				protected void updateItem(String item, boolean empty) {    // display graphic
					try {
						row = getTreeTableRow().getItem();
						if ((row.getIconId() > 0) || (row.getId() == 0)) {
							graphic = new ImageView(row.icon);
						} else {                                    // show default icon
							long iconIdDef = params.getConCur().db.sectionGetIconIdDefault(row.getParentId(), true);
							graphic = new ImageView(params.getConCur().db.iconGetImageById(iconIdDef));
						}
					} catch (NullPointerException e) {
						//e.printStackTrace();
						graphic = null;
					}

					super.updateItem(item, empty);
					setText(empty ? null : item);
					setGraphic(empty ? null : graphic);
				}
			});
		}

		/**
		 * RowFactory - for Drag&Drop
		 */
		void initRowFactory () {
			treeTableView_sections.setRowFactory(new Callback<TreeTableView<SectionItem>,
					TreeTableRow<SectionItem>>() {
				@Override
				public TreeTableRow<SectionItem> call(final TreeTableView<SectionItem> param) {
					final TreeTableRow<SectionItem> row = new TreeTableRow<SectionItem>();

					row.setOnDragDetected(new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							// drag was detected, start drag-and-drop gesture
							TreeItem<SectionItem> selected =
									(TreeItem<SectionItem>) treeTableView_sections.getSelectionModel().getSelectedItem();
							//SectionsItem_Parameters curIcon = selected.getValue();

							if (selected != null) {
								//Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
								Dragboard db = row.startDragAndDrop(TransferMode.ANY);

								// create a miniature of the row you're dragging
								db.setDragView(row.snapshot(null, null));

								// Keep whats being dragged on the clipboard
								ClipboardContent content = new ClipboardContent();
								//content.putString(selected.getValue().getName());
								content.put(params.getMain().SERIALIZED_MIME_TYPE, row.getIndex());
								db.setContent(content);

								event.consume();
							}
							//mainApp.statusBar_ShowMsg("setOnDragDetected");
						}
					});

					row.setOnDragOver(new EventHandler<DragEvent>() {
						@Override
						public void handle(DragEvent event) {
							// data is dragged over the target
							Dragboard db = event.getDragboard();

							if (dragAndDropAcceptable(db, row)) {
								event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
								event.consume();
							}
						}});

					row.setOnDragDropped(new EventHandler<DragEvent>() {
						@Override
						public void handle(DragEvent event) {
							Dragboard db = event.getDragboard();

							if (dragAndDropAcceptable(db, row)) {
								int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
								TreeItem<SectionItem> item = treeTableView_sections.getTreeItem(index);

								if (event.getAcceptedTransferMode() == TransferMode.MOVE) {
									if (ShowAppMsg.showQuestion("CONFIRMATION", "Перемещение раздела",
											"Перемещение раздела '"+ item.getValue().getName() +"'", "Переместить раздел ?")) {
										item.getParent().getChildren().remove(item);
										dragAndDropGetTarget(row).getChildren().add(item);
										event.setDropCompleted(true);
										treeTableView_sections.sort();

										//выбираем перемещенный итем по Id
										expandTreeItemsById(treeTableView_sections.getRoot(), item.getValue().getId());
										treeTableView_sections.getSelectionModel().select(item);

										// update in DB
										params.getConCur().db.sectionMove (item.getValue().getId(), dragAndDropGetTarget(row).getValue().getId());

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Раздел '" + item.getValue().getName() + "' перемещен.");
									}
								} else if (event.getAcceptedTransferMode() == TransferMode.COPY) {
									Preferences prefs = Preferences.userNodeForPackage(SectionList_Controller.class);
									boolean copyWithSubSections = prefs.get("copyWithSubSections", "No").equals("Yes");
									int retVal = ShowAppMsg.showQuestionWithOption(
											"CONFIRMATION", "Копирование раздела",
											"Копировать раздел '"+ item.getValue().getName() +"' ?", null,
											"Копировать ветку целиком", copyWithSubSections);
									long newSectionId = 0;

									if (retVal == ShowAppMsg.QUESTION_OK) {          // сохраняем только текущий итем
										event.setDropCompleted(true);

										newSectionId = copySection (item.getValue(), dragAndDropGetTarget(row), false);

										// save Option value
										prefs.put("copyWithSubSections", "No");

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Раздел '" + item.getValue().getName() + "' скопирован.");
									}
									if (retVal == ShowAppMsg.QUESTION_OK_WITH_OPTION) {          // сохраняем всю ветку
										event.setDropCompleted(true);

										// item                          - source TreeItem
										// getTarget_forDragAndDrop(row) - target parent TreeItem
										newSectionId = copySection (item.getValue(), dragAndDropGetTarget(row), true);

										// save Option value
										prefs.put("copyWithSubSections", "Yes");

										// выводим сообщение в статус бар
										params.setMsgToStatusBar("Раздел (ветка) '" + item.getValue().getName() + "' скопирован.");
									}
									treeTableView_sections.sort();
									expandTreeItemsById(root, newSectionId);
									selectTreeItemById(root, newSectionId);
								} else {
									ShowAppMsg.showAlert("WARNING", "Перетаскивание", "Не известный режим перетаскивания", "Не обрабатывается.");
								}
								event.consume();
							}
						}});

					return row;
				}
			});
		}

		/**
		 * восстанавливаем сортировку таблицы по столбцу
		 */
		public void initSortColumn () {
			String sortColumnId = prefs.get("SectionsList_sortColumnId","");

			if (! sortColumnId.equals("")) {
				for (TreeTableColumn column : treeTableView_sections.getColumns()) {
					if (column.getId().equals(sortColumnId)) {
						String sortType = prefs.get("SectionsList_sortType","ASCENDING");

						treeTableView_sections.setSortMode(TreeSortMode.ALL_DESCENDANTS);
						column.setSortable(true); // This performs a sort
						treeTableView_sections.getSortOrder().add(column);
						if (sortType.equals("DESCENDING")) column.setSortType(TreeTableColumn.SortType.DESCENDING);
						else                               column.setSortType(TreeTableColumn.SortType.ASCENDING);
						treeTableView_sections.sort();
					}
				}
			}
		}

		/**
		 * Возвращает Истину, если перетаскивание возможно, иначе Ложь.
		 */
		private boolean dragAndDropAcceptable(Dragboard db, TreeTableRow<SectionItem> row) {
			boolean result = false;
			if (db.hasContent(params.getMain().SERIALIZED_MIME_TYPE)) {
				int index = (Integer) db.getContent(params.getMain().SERIALIZED_MIME_TYPE);
				if (row.getIndex() != index) {
					TreeItem<SectionItem> target = dragAndDropGetTarget(row);
					TreeItem<SectionItem> item = treeTableView_sections.getTreeItem(index);
					result = !dragAndDropIsParent(item, target);
				}
			}
			return result;
		}

		/**
		 * Получаем строчку-приемник при перетаскивании
		 */
		private TreeItem<SectionItem> dragAndDropGetTarget(TreeTableRow<SectionItem> row) {
			TreeItem<SectionItem> target = treeTableView_sections.getRoot();
			if (!row.isEmpty()) {
				target = row.getTreeItem();
			}
			return target;
		}

		/**
		 * prevent loops in the tree
		 */
		private boolean dragAndDropIsParent(TreeItem<SectionItem> parent, TreeItem<SectionItem> child) {
			boolean result = false;
			while (!result && child != null) {
				result = child.getParent() == parent;
				child = child.getParent();
			}
			return result;
		}

		/*
		 * Возвращает строку иерархического пути раздела
		 *
		 * ti - текущий раздел
		 * level - 0 - показывает без текущего раздела ; 1 - с текущим разделом
		 */
		public String getSectionPath (TreeItem<SectionItem> ti, int level) {
			String msg = null;
			boolean isFirst = true;

			if (ti != null) {
				SectionItem f = (SectionItem) ti.getValue();

				// show section path in StatusBar
				if (level == 1) msg = new String(f.getName());
				//if (f.getId() > 0) {
				if (f.getId() != rootSectionId) {
					TreeItem<SectionItem> curTI = ti.getParent();
					SectionItem curSection = curTI.getValue();

					//while (curSection.getId() >= 0) {
					while (curSection.getId() != rootSectionId) {
						if ((level == 1) || (! isFirst)) msg = curSection.getName() + " / " + msg;
						else {
							msg = curSection.getName();
							isFirst = false;
						}

						if (curSection.getId() == 0)   break;
						else {
							curTI = curTI.getParent();
							curSection = curTI.getValue();
						}
					}
					
					msg = curSection.getName() + " / " + msg;
				}
			}
			return msg;
		}

		/**
		 * Раскрываем в дереве разделов раздел по его Id
		 */
		private int expandTreeItemsById(TreeItem<SectionItem> ti, long selId) {
			SectionItem si = ti.getValue();

			if (si.getId() == selId) {
				//treeTableView_sections.getSelectionModel().select(ti);
				return 1;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<SectionItem> i : ti.getChildren()) {
				if (expandTreeItemsById(i, selId) == 1) {
					ti.setExpanded(true);
					return 1;
				}
			}

			return 0;
		}
		
		/**
		 * Выбираем в дереве разделов раздел по его Id
		 */
		public TreeItem<SectionItem> getTreeItemById(TreeItem<SectionItem> ti, long selId) {
			SectionItem si = ti.getValue();

			if (si.getId() == selId) {
				return ti;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<SectionItem> i : ti.getChildren()) {
				TreeItem<SectionItem> tsi = getTreeItemById(i, selId);
				
				if (tsi != null)
					return tsi;
			}

			return null;
		}

		/**
		 * Выбираем в дереве разделов текущий раздел по его Id
		 */
		int selectTreeItemById(TreeItem<SectionItem> ti, long selId) {
			SectionItem si = ti.getValue();

			if (si.getId() == selId) {
				treeTableView_sections.getSelectionModel().select(ti);
				return 1;
			}

			//-------- выбираем дочерние итемы и запускаем рекурсию
			for (TreeItem<SectionItem> i : ti.getChildren()) {
				if (selectTreeItemById(i, selId) == 1)
					return 1;
			}

			return 0;
		}

		/**
		 * Копируем рекурсивно ветку разделов или один раздел
		 * cpyCII - копируемый раздел
		 * trgTI - элемент дерева-раздела, в который копировать
		 */
		private long copySection (
				SectionItem cpyCII, TreeItem<SectionItem> trgTI, boolean isRecursive) {

			if (trgTI.getValue() == null)  return 0;

			// создаем новый обьект раздела для копирования
			SectionItem curCII = new SectionItem(cpyCII);
			curCII.setId(params.getConCur().db.sectionNextId());
			curCII.setParentId(trgTI.getValue().getId());
			//curCII.setDateCreated(new Date());
			//curCII.setDateModified(new Date());
			//curCII.setDateModifiedInfo(new Date());

			// раздел и его инфо блоки добавляем в БД
			params.getConCur().db.sectionAdd(curCII);
			params.getConCur().db.sectionCopyInfoBlocks(cpyCII.getId(), curCII.getId());
			//params.getConCur().db.sectionUpdateDateModifiedInfo(curCII.getId());

			// добавляем к новому родителю
			TreeItem<SectionItem> curTI = new TreeItem<>(curCII);
			trgTI.getChildren().add(curTI);

			// в цикле вызываем рекурсивный метод
			if (isRecursive) {
				List<SectionItem> sectionsList = params.getConCur().db.sectionListByParentId(cpyCII.getId());

				for (SectionItem i : sectionsList) {
					copySection (i, curTI, isRecursive);
				}
			}
			
			return curCII.getId();
		}
	}
}
