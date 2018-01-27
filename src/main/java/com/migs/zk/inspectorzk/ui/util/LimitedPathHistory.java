package com.migs.zk.inspectorzk.ui.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by migc on 1/15/18.
 */
public class LimitedPathHistory {

	private static Logger log = LogManager.getLogger(LimitedPathHistory.class);

	private LinkedList<String> historyList;
	private int limit;
	private int cursor;

	private boolean fromBack;
	private boolean fromForward;
	private SimpleBooleanProperty atFront;
	private SimpleBooleanProperty atBack;
	private SimpleIntegerProperty historyCount;

	public LimitedPathHistory(int historyLimit) {
		historyList = new LinkedList<>();
		limit = historyLimit;
		cursor = 0;
		atFront = new SimpleBooleanProperty(true);
		atBack = new SimpleBooleanProperty(true);

		historyCount = new SimpleIntegerProperty(historyList.size());
	}

	public void addPath(String path){
		if(fromBack || fromForward) {
			fromBack = false;
			fromForward = false;
		}
		else {
			if(historyList.size() == limit) {
				historyList.removeFirst();
				atBack.set(true);
			}

			if(historyList.size() == 0) {
				historyList.add(path);
				atFront.set(true);
			}
			else if(historyList.size() >= 1){
				String prevPath = historyList.getLast();

				if(!prevPath.equals(path))
					historyList.add(path);

				atFront.set(false);
			}

			historyCount.set(historyList.size());
			cursor = historyList.size() - 1;

			log.debug("cursor: "+ cursor);
			log.debug(this);
		}
	}

	public String getPreviousPath(){
		if(historyList.size() <= 1) {
			atFront.set(true);
			atBack.set(false);
			return historyList.getFirst();
		}
		else{
			--cursor;
			if(cursor <= 0) {
				cursor = 0;
				atFront.set(true);
				atBack.set(false);
			}
			else {
				atFront.set(false);
				atBack.set(false);
			}

			fromBack = true;
			return historyList.get(cursor);
		}
	}

	public void deleteCurrentPath(){
		historyList.removeLast();
	}

	public void clearHistory(){
		cursor = 0;
		historyList.clear();
		historyCount.set(0);
		atFront.set(true);
		atBack.set(true);
	}

	public SimpleIntegerProperty getCurrentHistorySize(){
		return historyCount;
	}

	public String getNextPath(){
		if(historyList.size() < 1) {
			atBack.set(true);
			atFront.set(false);
			return historyList.getLast();
		}
		else{
			++cursor;

			if(cursor >=  historyList.size()-1) {
				cursor = historyList.size() - 1;
				atBack.set(true);
				atFront.set(false);
			}
			else {
				atBack.set(false);
				atFront.set(false);
			}

			fromForward = true;
			return historyList.get(cursor);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		int i=0;
		Iterator<String> pathIter = historyList.iterator();

		while(pathIter.hasNext()){
			sb.append(String.format("%d. %s\n", ++i, pathIter.next()));
		}

		return sb.toString();
	}

	public String gotoFirst(){
		cursor = 0;
		atFront.set(true);
		atBack.set(false);
		return historyList.getFirst();
	}

	public String gotoLast(){
		cursor = historyList.size()-1;
		atBack.set(true);
		atFront.set(false);
		return historyList.getLast();
	}

	public boolean isAtFront() {
		return atFront.get();
	}

	public SimpleBooleanProperty atFrontProperty() {
		return atFront;
	}

	public boolean isAtBack() {
		return atBack.get();
	}

	public SimpleBooleanProperty atBackProperty() {
		return atBack;
	}
}
