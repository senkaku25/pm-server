package com.pm.server.player;

import org.springframework.stereotype.Component;

@Component
public class PacmanImpl extends PlayerImpl implements Pacman {

	public PacmanImpl() {
		this.id = 0;
	}

}
