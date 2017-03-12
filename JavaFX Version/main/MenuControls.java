package main;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class MenuControls extends MenuBar {
	
	public MenuControls(){
		Menu file = new Menu("File");
		
		MenuItem save = new MenuItem("Save As");
		
		MenuItem dsf = new MenuItem("Default save folder");
		
		CheckMenuItem autosave = new CheckMenuItem("Autosave");
		
		MenuItem reset = new MenuItem("Reset");
		
		file.getItems().addAll(save, dsf, autosave, new SeparatorMenuItem(), reset);
		
		
		
		Menu edit = new Menu("Edit");
		
		MenuItem zoom = new MenuItem("Zoom");
		
		MenuItem iterations = new MenuItem("Iterations");
		
		MenuItem offsets = new MenuItem("Offsets");
		
		MenuItem size = new MenuItem("Image Size");
		
		edit.getItems().addAll(zoom, iterations, offsets, new SeparatorMenuItem(), size);
		
		
		
		Menu render = new Menu("Render");
		
		MenuItem rend = new MenuItem("Render");
		
		MenuItem stop = new MenuItem("Abort Render");
		
		MenuItem threads = new MenuItem("Threads");
		
		render.getItems().addAll(rend, stop, new SeparatorMenuItem(), threads);
		
		this.getMenus().addAll(file, edit, render);
	}

}
