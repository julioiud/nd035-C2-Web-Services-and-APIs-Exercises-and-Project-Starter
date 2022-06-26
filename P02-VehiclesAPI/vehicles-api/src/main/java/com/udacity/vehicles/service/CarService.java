package com.udacity.vehicles.service;

import com.udacity.vehicles.client.prices.Price;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.CarRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.swing.text.html.Option;

/**
 * Implements the car service create, read, update or delete
 * information about vehicles, as well as gather related
 * location and price data when desired.
 */
@Service
public class CarService implements ICardService{

    private final CarRepository repository;

    private final WebClient webClientMaps;
    private final WebClient webClientPricing;

    @Autowired
    public CarService(CarRepository repository,
                      @Qualifier("maps") WebClient webClientMaps,
                      @Qualifier("pricing") WebClient webClientPricing) {
        this.webClientMaps = webClientMaps;
        this.webClientPricing = webClientPricing;

        this.repository = repository;
    }

    /**
     * Gathers a list of all vehicles
     * @return a list of all vehicles in the CarRepository
     */
    @Override
    public List<Car> list() {
        return repository.findAll();
    }

    /**
     * Gets car information by ID (or throws exception if non-existent)
     * @param id the ID number of the car to gather information on
     * @return the requested car's information, including location and price
     */
    @Override
    public Car findById(Long id) {

        var cardOptional = repository.findById(id);

        if(cardOptional.isEmpty()){
            throw new CarNotFoundException("Card Not Found");
        }
        Car car = cardOptional.get();
        /**
         * Note: The car class file uses @transient, meaning you will need to call
         *   the pricing service each time to get the price.
         */
        var price =
                webClientPricing.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/services/price")
                                .queryParam("vehicleId", id)
                                .build())
                        .retrieve()
                        .bodyToMono(Price.class)
                        .block();

        car.setPrice(price.getPrice().toString());

        /**
         * Note: The Location class file also uses @transient for the address,
         * meaning the Maps service needs to be called each time for the address.
         */
        var location = webClientMaps.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/maps")
                        .queryParam("lat", car.getLocation().getLat().toString())
                        .queryParam("lon", car.getLocation().getLon().toString())
                        .build())
                .retrieve()
                .bodyToMono(Location.class)
                .block();

        Location locationSet = new Location(car.getLocation().getLat(), car.getLocation().getLon());
        locationSet.setAddress(location.getAddress());
        locationSet.setCity(location.getCity());
        locationSet.setState(location.getState());
        locationSet.setZip(location.getZip());
        car.setLocation(locationSet);

        return car;
    }

    /**
     * Either creates or updates a vehicle, based on prior existence of car
     * @param car A car object, which can be either new or existing
     * @return the new/updated car is stored in the repository
     */
    @Override
    public Car save(Car car) {
        if (car.getId() != null) {
            return repository.findById(car.getId())
                    .map(carToBeUpdated -> {
                        carToBeUpdated.setDetails(car.getDetails());
                        carToBeUpdated.setLocation(car.getLocation());
                        return repository.save(carToBeUpdated);
                    }).orElseThrow(CarNotFoundException::new);
        }

        return repository.save(car);
    }

    /**
     * Deletes a given car by ID
     * @param id the ID number of the car to delete
     */
    @Override
    public void delete(Long id) {
        var cardOptional = repository.findById(id);
        if(cardOptional.isEmpty()){
            throw new CarNotFoundException("Card Not Found");
        }
        Car car = cardOptional.get();
        repository.delete(car);
    }
}
