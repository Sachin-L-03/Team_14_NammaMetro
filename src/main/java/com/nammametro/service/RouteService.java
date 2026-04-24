package com.nammametro.service;

import com.nammametro.model.Route;
import com.nammametro.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Business logic layer for Route operations.
 * Implements IRouteService (ISP — Interface Segregation Principle).
 *
 * SRP: This class has one responsibility — encapsulating business rules for routes.
 */
@Service
public class RouteService implements IRouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public List<Route> findAll() {
        return routeRepository.findAll();
    }

    @Override
    public Optional<Route> findById(Long id) {
        return routeRepository.findById(id);
    }

    @Override
    public Route save(Route route) {
        return routeRepository.save(route);
    }

    @Override
    public void deleteById(Long id) {
        routeRepository.deleteById(id);
    }

    @Override
    public long count() {
        return routeRepository.count();
    }
}
