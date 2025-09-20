#!/bin/bash
#
# @path scripts/test-flyway-migrations.sh
# @description Script de test des migrations Flyway pour validation de la Phase 3
#

echo "=== Test des migrations Flyway Phase 3 ==="

# Arrêter les conteneurs existants
docker-compose down -v

# Supprimer les volumes pour partir d'une base propre
docker volume prune -f

# Démarrer uniquement la base de données
docker-compose up -d db

# Attendre que MySQL soit prêt
echo "Attente de MySQL..."
until docker exec gc-db mysqladmin ping -h localhost --silent; do
    sleep 2
done

# Construire l'application avec profil test
echo "Construction de l'application..."
cd gestion-commerciale-backend
./mvnw clean compile -Dspring.profiles.active=test

# Lancer Flyway pour tester les migrations
echo "Exécution des migrations Flyway..."
./mvnw flyway:migrate -Dspring.profiles.active=test

# Vérifier le statut des migrations
echo "Statut des migrations :"
./mvnw flyway:info -Dspring.profiles.active=test

echo "=== Test terminé ==="
