package to2.dice.GUI.controllers;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;

import to2.dice.GUI.model.Model;
import to2.dice.GUI.views.GameListView;
import to2.dice.GUI.views.View;
import to2.dice.game.GameState;
import to2.dice.game.Player;
import to2.dice.messaging.Response;
import to2.dice.server.ServerMessageListener;

public class GameController extends Controller implements ServerMessageListener {
	private static final int timeForAnimInMiliseconds = 1500;
	
	private GameAnimController gameAnimController;
	private Player lastPlayer = null;
	private int lastRound;

	public GameController(Model model, GameAnimController gameAnimController) {
		super(model);
		this.gameAnimController = gameAnimController;
		this.lastRound = -1;
	}

	public void rerollDice() {
		try {
			Response response = model.getConnectionProxy().reroll(model.getSelectedDice());
			if (response.isSuccess()) {
				model.setSelectedDice(new boolean[model.getGameSettings().getDiceNumber()]);
			} else {
				view.showErrorDialog(response.message, "B��d wychodzenia", false);
			}
		} catch (TimeoutException e) {
			e.printStackTrace();
			view.showErrorDialog("Utracono po��czenie z serwerem", "B��d po��czenia", true);
		}
	}

	public void clickedStandUpLeaveButton() {
		if (model.isSitting() && model.getGameState().isGameStarted()) {
			try {
				Response response = model.getConnectionProxy().standUp();
				if (response.isSuccess()) {
					model.setSitting(false);
					view.refresh();
				} else {
					view.showErrorDialog("Nie uda�o si� wsta�", "B��d wstawania", false);
				}
			} catch (TimeoutException e) {
				e.printStackTrace();
				view.showErrorDialog("Utracono po��czenie z serwerem", "B��d po��czenia", true);
			}
		} else if (!model.isSitting() && model.getGameState().isGameStarted()) {
			try {
				Response response = model.getConnectionProxy().leaveRoom();
				if (response.isSuccess()) {
					lastRound = -1;
					lastPlayer = null;
					model.setSitting(false);
					GameListController newController = new GameListController(model);
					model.getServerMessageContainer().removeServerMessageListener();
					model.setGameSettings(null);
					model.setGameState(new GameState());
					GameListView newView = new GameListView(model, newController);
					newController.setView(newView);
					newController.refreshGameList();
					model.getDiceApplication().setView(newView);
				} else {
					view.showErrorDialog("Nie uda�o si� wyj��", "B��d wstawania", false);
				}
			} catch (TimeoutException e) {
				e.printStackTrace();
				view.showErrorDialog("Utracono po��czenie z serwerem", "B��d po��czenia", true);
			}
		} else {
			lastRound = -1;
			lastPlayer = null;
			model.setSitting(false);
			GameListController newController = new GameListController(model);
			model.getServerMessageContainer().removeServerMessageListener();
			model.setGameSettings(null);
			model.setGameState(new GameState());
			GameListView newView = new GameListView(model, newController);
			newController.setView(newView);
			newController.refreshGameList();
			model.getDiceApplication().setView(newView);
		}

	}

	// TODO koniec gry
	public void onGameStateChange(GameState gameState) {
		model.setGameState(gameState);
		if (lastPlayer == null && lastRound == -1) {
			startGame(gameState);
		}
		if (checkKickout(gameState)) {
			model.setSitting(false);
			// TODO remove user dice and box
		}
		if (!gameState.isGameStarted()) {
			endGame(gameState);
		} else if (gameState.getCurrentPlayer() == null) {
			endTour(gameState);
		} else if (lastPlayer == null) {
			nextRound(gameState);
		} else if (!lastPlayer.equals(gameState.getCurrentPlayer())) {
			endTourNextTour(gameState);
		} else {
			// TODO nothing is changed
		}
		model.getDiceApplication().refresh();
	}

	private boolean checkKickout(GameState gameState) {
		for (Player p: gameState.getPlayers()) {
			if (p.getName().equals(model.getLogin()) ) {
				return false;
			}
		}
		return false;
	}

	private void endTourNextTour(GameState gameState) {
		endTour(gameState);
		new Timer().schedule(new TimerTask() {
			
			@Override
			public void run() {
				if (model.isMyTurn()) {
					// teraz jest nasza tura
					gameAnimController.hideAnotherDice();
					gameAnimController.showText("Twoja tura", 0);
				} else {
					gameAnimController.showAnotherDice();
					gameAnimController.putAnotherDice(gameState.getCurrentPlayer().getDice());
					gameAnimController.showText("", 0);
				}
				if (model.isSitting()) {
					for (Player p : gameState.getPlayers()) {
						if (p.getName().equals(model.getLogin())) {
							gameAnimController.putUserDice(p.getDice());
							break;
						}
					}
				}
				model.setTimer(model.getGameSettings().getTimeForMove());
				
			}
		}, timeForAnimInMiliseconds);
	}

	private void nextRound(GameState gameState) {
		// TODO pokaza� �adny napis na �rodku :)
		lastRound += 1;
		lastPlayer = gameState.getCurrentPlayer();
		if (model.isMyTurn()) {
			gameAnimController.hideAnotherDice();
		} else {
			gameAnimController.showAnotherDice();
			gameAnimController.putAnotherDice(gameState.getCurrentPlayer().getDice());
		}
		if (model.isSitting()) {
			for (Player p : gameState.getPlayers()) {
				if (p.getName().equals(model.getLogin())) {
					gameAnimController.putUserDice(p.getDice());
					break;
				}
			}
		}
	}

	private void endTour(GameState gameState) {
		for (Player p : gameState.getPlayers()) {
			if (p.equals(lastPlayer)) {
				if (p.getName().equals(model.getLogin())) {
					gameAnimController.shakeUserDice(p.getDice());
				} else {
					gameAnimController.shakeAnotherDice(p.getDice());
				}
			}
		}
		lastPlayer = gameState.getCurrentPlayer();
	}

	private void startGame(GameState gameState) {
		lastPlayer = gameState.getCurrentPlayer();
		if (model.isMyTurn()) {
			gameAnimController.hideAnotherDice();
		} else {
			gameAnimController.showAnotherDice();
			gameAnimController.putAnotherDice(gameState.getCurrentPlayer().getDice());
		}
		if (model.isSitting()) {
			for (Player p : gameState.getPlayers()) {
				if (p.getName().equals(model.getLogin())) {
					gameAnimController.putUserDice(p.getDice());
					break;
				}
			}
		}
		lastRound = 1;
	}

	private void endGame(GameState gameState) {
		this.lastRound = -1;
		this.lastPlayer = null;
	}

}
