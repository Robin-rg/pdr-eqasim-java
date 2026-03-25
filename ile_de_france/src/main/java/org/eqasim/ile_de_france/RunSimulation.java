package org.eqasim.ile_de_france;

import org.eqasim.core.scenario.validation.VehiclesValidator;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.CommandLine;
import org.matsim.core.config.CommandLine.ConfigurationException;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;

/* == DRS == */
import org.matsim.core.config.ConfigGroup;

import at.ac.ait.matsim.drs.run.Drs;
import at.ac.ait.matsim.drs.run.DrsConfigGroup;
import at.ac.ait.matsim.drs.util.CarLinkAssigner;
import at.ac.ait.matsim.drs.util.DrsUtil;
/* ========= */

public class RunSimulation {
	static public void main(String[] args) throws ConfigurationException {
		CommandLine cmd = new CommandLine.Builder(args) //
				.requireOptions("config-path") //
				.allowPrefixes("mode-choice-parameter", "cost-parameter") //
				.build();

		IDFConfigurator configurator = new IDFConfigurator(cmd);
		Config config = ConfigUtils.loadConfig(cmd.getOptionStrict("config-path"), new ConfigGroup[]{new DrsConfigGroup()} );
		configurator.updateConfig(config);

		cmd.applyConfiguration(config);
		VehiclesValidator.validate(config);

		Scenario scenario = ScenarioUtils.createScenario(config);
		configurator.configureScenario(scenario);
		ScenarioUtils.loadScenario(scenario);
		configurator.adjustScenario(scenario);

        /* == DRS == */
        (new CarLinkAssigner(scenario.getNetwork())).run(scenario.getPopulation());
        DrsUtil.addMissingCoordsToPlanElementsFromLinks(scenario.getPopulation(), scenario.getNetwork());
        DrsUtil.addNewAllowedModeToCarLinks(scenario.getNetwork(), "drsDriver");

        DrsUtil.addFakeGenericRouteToDrsDriverLegs(scenario.getPopulation());
        /* ========= */

		Controler controller = new Controler(scenario);
		configurator.configureController(controller);

		/* == DRS == */
        Drs.prepareController(controller);
        /* ========= */

		controller.run();
	}
}