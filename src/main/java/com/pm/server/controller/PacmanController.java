package com.pm.server.controller;

import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.pm.server.datatype.Coordinate;
import com.pm.server.datatype.CoordinateImpl;
import com.pm.server.exceptionhttp.BadRequestException;
import com.pm.server.exceptionhttp.ConflictException;
import com.pm.server.exceptionhttp.InternalServerErrorException;
import com.pm.server.exceptionhttp.NotFoundException;
import com.pm.server.player.Pacman;
import com.pm.server.player.PacmanImpl;
import com.pm.server.repository.PacmanRepository;
import com.pm.server.response.LocationResponse;

@RestController
@RequestMapping("/pacman")
public class PacmanController {

	@Autowired
	private PacmanRepository pacmanRepository;

	private final static Logger log =
			LogManager.getLogger(PacmanController.class.getName());

	@RequestMapping(
			value = "/{latitude}/{longitude}",
			method = RequestMethod.POST
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void createPacman(
			@PathVariable double latitude,
			@PathVariable double longitude)
			throws ConflictException, InternalServerErrorException {

		log.debug("Mapped POST /pacman/{}/{}", latitude, longitude);

		if(pacmanRepository.getPlayer() != null) {
			String errorMessage = "A Pacman already exists.";
			log.warn(errorMessage);
			throw new ConflictException(errorMessage);
		}

		Pacman pacman = new PacmanImpl();
		pacman.setLocation(new CoordinateImpl(latitude, longitude));

		try {
			pacmanRepository.addPlayer(pacman);
		}
		catch(Exception e) {
			log.error(e.getMessage());
			throw new InternalServerErrorException(e.getMessage());
		}

	}

	@RequestMapping(
			value="/location",
			method=RequestMethod.GET
	)
	@ResponseStatus(value = HttpStatus.OK)
	public LocationResponse getPacmanLocation(
			HttpServletResponse response)
			throws NotFoundException, InternalServerErrorException {

		log.debug("Mapped GET /pacman/location");

		Pacman pacman = pacmanRepository.getPlayer();
		if(pacman == null) {
			String errorMessage = "No Pacman exists";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		Coordinate coordinate = pacman.getLocation();
		if(coordinate == null) {
			String errorMessage =
					"The location of the Pacman could not be extracted.";
			log.error(errorMessage);
			throw new InternalServerErrorException(errorMessage);
		}

		LocationResponse locationResponse = new LocationResponse();
		locationResponse.setLatitude(coordinate.getLatitude());
		locationResponse.setLongitude(coordinate.getLongitude());

		return locationResponse;

	}

	@RequestMapping(
			value="/location/{latitude}/{longitude}",
			method=RequestMethod.PUT
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void setPacmanLocation(
			@PathVariable double latitude,
			@PathVariable double longitude,
			HttpServletResponse response)
			throws NotFoundException {

		log.debug("Mapped PUT /pacman/location/{}/{}", latitude, longitude);

		Pacman pacman = pacmanRepository.getPlayer();
		if(pacman == null) {
			String errorMessage = "No Pacman exists.";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		log.debug(
				"Setting Pacman at location ({}, {}) to location ({}, {})",
				pacman.getLocation().getLatitude(),
				pacman.getLocation().getLongitude(),
				latitude, longitude
		);
		pacman.setLocation(new CoordinateImpl(latitude, longitude));
	}

	@RequestMapping(
			method=RequestMethod.DELETE
	)
	@ResponseStatus(value = HttpStatus.OK)
	public void deletePacman(
			HttpServletResponse response)
			throws NotFoundException {

		log.debug("Mapped DELETE /pacman");

		if(pacmanRepository.getPlayer() == null) {
			String errorMessage = "No Pacman exists.";
			log.warn(errorMessage);
			throw new NotFoundException(errorMessage);
		}

		pacmanRepository.clearPlayers();
	}

	private static void validateRequestBodyWithLocation(Coordinate location)
			throws BadRequestException {

		String errorMessage = null;

		if(location == null) {
			errorMessage = "Request body requires latitude and longitude.";
		}
		else if(
				location.getLatitude() == null &&
				location.getLongitude() == null) {
			errorMessage = "Request body requires latitude and longitude.";
		}
		else if(location.getLatitude() == null) {
			errorMessage = "Request body requires latitude.";
		}
		else if(location.getLongitude() == null) {
			errorMessage = "Request body requires longitude.";
		}

		if(errorMessage != null) {
			log.warn(errorMessage);
			throw new BadRequestException(errorMessage);
		}

	}

}
