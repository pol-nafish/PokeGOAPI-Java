package com.pokegoapi.api.device;

import com.pokegoapi.api.PokemonGo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import POGOProtos.Networking.Envelopes.SignatureOuterClass;

/**
 * Created by fabianterhorst on 23.08.16.
 */

public class LocationFix {

	private SignatureOuterClass.Signature.LocationFix.Builder locationFixBuilder;

	public LocationFix() {
		locationFixBuilder = SignatureOuterClass.Signature.LocationFix.newBuilder();
	}

	public SignatureOuterClass.Signature.LocationFix.Builder getBuilder() {
		return locationFixBuilder;
	}

	/**
	 * Gets the default device info for the given api
	 *
	 * @param api the api
	 * @return the default device info for the given api
	 */
	public static List<SignatureOuterClass.Signature.LocationFix> getDefault(PokemonGo api) {
		Random random = new Random();
		int pn = random.nextInt(100);
		int nProviders;
		HashSet<String> negativeSnapshotProviders = new HashSet<>();

		if (api.isFirstLocationFix()) {
			api.setFirstLocationFix(false);
			nProviders = pn < 75 ? 6 : pn < 95 ? 5 : 8;

			if (nProviders != 8) {
				// a 5% chance that the second provider got a negative value else it should be the first only
				int nChanche = random.nextInt(100);
				negativeSnapshotProviders.add(nChanche < 95 ? "0" : "1");
			} else {
				int nChanche = random.nextInt(100);
				if (nChanche >= 50) {
					negativeSnapshotProviders.add("0");
					negativeSnapshotProviders.add("1");
					negativeSnapshotProviders.add("2");
				} else {
					negativeSnapshotProviders.add("0");
					negativeSnapshotProviders.add("1");
				}
			}
		} else {
			nProviders = pn < 60 ? 1 : pn < 90 ? 2 : 3;
		}

		List<SignatureOuterClass.Signature.LocationFix> locationFixes = new ArrayList<>(nProviders);

		for (int i = 0; i < nProviders; i++) {
			float latitude = offsetOnLatLong(api.getLatitude(), random.nextInt(100) + 10);
			float longitude = offsetOnLatLong(api.getLongitude(), random.nextInt(100) + 10);
			float altitude = 65;
			float verticalAccuracy = (float) (15 + (23 - 15) * random.nextDouble());

			// Fake errors xD
			if (random.nextInt(100) > 90) {
				latitude = 360;
				longitude = -360;
			}

			// Another fake error
			if (random.nextInt(100) > 90) {
				altitude = (float) (66 + (160 - 66) * random.nextDouble());
			}

			SignatureOuterClass.Signature.LocationFix.Builder locationFixBuilder =
					SignatureOuterClass.Signature.LocationFix.newBuilder();

			locationFixBuilder.setProvider("fused")
					.setTimestampSnapshot(negativeSnapshotProviders.contains(String.valueOf(i)) ?
							random.nextInt(1000) - 3000 : api.currentTimeMillis() - api.getStartTime())
					.setLatitude(latitude)
					.setLongitude(longitude)
					.setHorizontalAccuracy(-1)
					.setAltitude(altitude)
					.setVerticalAccuracy(verticalAccuracy)
					.setProviderStatus(3)
					.setLocationType(1);
			locationFixes.add(locationFixBuilder.build());
		}
		return locationFixes;
	}

	private static float offsetOnLatLong(double l, double d) {
		double r = 6378137;
		double dl = d / (r * Math.cos(Math.PI * l / 180));
		return (float) (l + dl * 180 / Math.PI);
	}
}
