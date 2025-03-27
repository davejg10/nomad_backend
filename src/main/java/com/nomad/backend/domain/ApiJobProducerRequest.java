package com.nomad.backend.domain;

import java.time.LocalDate;

public record ApiJobProducerRequest(CityDTO sourceCity, CityDTO targetCity, LocalDate searchDate) {}
