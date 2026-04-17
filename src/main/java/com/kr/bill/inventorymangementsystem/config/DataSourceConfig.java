package com.kr.bill.inventorymangementsystem.config;

// DataSource URL conversion is handled by DatabaseUrlEnvironmentPostProcessor
// which runs before any Spring beans are created.

/**
 * Placeholder configuration class for DataSource setup.
 *
 * <p>The actual DataSource URL conversion is performed <em>before</em> any
 * Spring beans are created by
 * {@link DatabaseUrlEnvironmentPostProcessor}, which is registered as a
 * {@code org.springframework.boot.env.EnvironmentPostProcessor} in
 * {@code META-INF/spring/org.springframework.boot.env.EnvironmentPostProcessor}.</p>
 *
 * <p>This class exists to satisfy structural conventions; no beans are
 * declared here.</p>
 *
 * @see DatabaseUrlEnvironmentPostProcessor
 */
public class DataSourceConfig {
}