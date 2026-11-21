# WanderWise — Smart Travel Planner

A Java 21 + Spring Boot travel-planning web app. The Spring Boot server serves the website and REST API from one origin.

## Included
- Destination catalogue: Goa, Jaipur, Manali, Kerala, Varanasi and Rishikesh.
- Working rule-based recommendations: multi-select interests, style ranking, total budget divided by days and travellers, cost estimates.
- Live nearby-place discovery through OpenStreetMap Overpass: attractions, hotels/stays, restaurants/cafes, essential amenities (hospitals, pharmacies, ATMs, fuel, parking, toilets, police, etc.) and public transport stops.
- Live current weather and seven-day forecast through Open-Meteo.
- Day-wise itinerary preview.
- Save/list/delete trips through Spring Data JPA; H2 locally and PostgreSQL when deployed.
- Transport, accommodation and dining search links to external providers.
- Dockerfile and Render blueprint for deployment.

## Run locally
Requirements: JDK 21 and Maven 3.9+.

```bash
mvn spring-boot:run
```

Open http://localhost:8080. Health check: http://localhost:8080/api/health.

You can also build and run:

```bash
mvn clean package
java -jar target/wanderwise-1.0.0.jar
```

## API endpoints
- `GET /api/health`
- `GET /api/destinations`
- `GET /api/recommendations?budget=30000&days=4&travellers=2&preferences=Beach&preferences=Food&style=Any`
- `GET /api/weather/{destinationId}`
- `GET /api/places/{destinationId}?category=attractions|hotels|restaurants|amenities|transport|all`
- `GET /api/transport-links/{destinationId}`
- `GET /api/booking-links/{destinationId}`
- `GET /api/trips`, `POST /api/trips`, `DELETE /api/trips/{id}`

## Deploy publicly
1. Push this folder to a GitHub repository.
2. Create a Render account and choose **New → Blueprint**, then connect the repository containing `render.yaml`.
3. Render provisions the Docker web service and PostgreSQL database, and configures the production Spring profile with PostgreSQL connection settings. Wait for deploy to finish.
4. Open the generated `https://...onrender.com` URL. The `/api/health` endpoint should return `status: UP`.

The free hosting/database options and provider terms can change. A free instance may sleep when idle; the first request after sleep can take longer. Keep database credentials in host environment variables; never commit secrets.

## Important limits / booking clarification
- This project discovers mapped nearby places; OpenStreetMap data is community-maintained and can be incomplete. Public Overpass instances have rate limits, so avoid rapid repeated searches. Attribute OpenStreetMap contributors in public use.
- Weather comes from Open-Meteo; availability and service limits are governed by that provider.
- Transport, hotel and restaurant buttons are outbound search links. They do **not** reserve seats/rooms, take payments, or confirm a booking. True in-app booking requires commercial provider APIs, credentials/partner approval, pricing/availability contracts, payment handling, cancellation/refund workflows and security review. This build does not pretend those integrations exist.
- No login or user-specific access control is included yet. Anyone with access to the deployed site can see the shared saved-trip list. Add authentication and per-user authorization before public multi-user use.
