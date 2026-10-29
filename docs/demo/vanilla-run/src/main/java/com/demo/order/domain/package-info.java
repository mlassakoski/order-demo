/**
 * Domain layer: aggregates, value objects, domain exceptions.
 *
 * <p>Pure Java only. No Spring, Jakarta or Lombok. State changes happen through
 * aggregate methods (for example {@code order.cancel()}), never through setters.
 * Business rule violations throw domain exceptions.
 */
package com.demo.order.domain;
