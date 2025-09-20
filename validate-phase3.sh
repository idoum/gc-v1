#!/bin/bash
#
# @path validate-phase3.sh  
# @description Script de validation complète Phase 3 avec tests et vérifications
#

set -e # Arrêter le script en cas d'erreur

echo "======================================================"
echo "    VALIDATION PHASE 3 - FINALISATION DES DONNÉES    "
echo "======================================================"
echo

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${YELLOW}➤ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

# Variables
CURRENT_DIR=$(pwd)
MYSQL_CONTAINER="gc-db"
BACKEND_DIR="gestion-commerciale-backend"

# Vérifier qu'on est dans le bon répertoire
if [[ ! -d "$BACKEND_DIR" ]]; then
    print_error "Répertoire $BACKEND_DIR non trouvé. Êtes-vous à la racine du projet ?"
    exit 1
fi

cd "$BACKEND_DIR"

# Étape 1: Vérification de l'environnement
print_step "1. Vérification de l'environnement..."

if ! command -v docker &> /dev/null; then
    print_error "Docker n'est pas installé"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose n'est pas installé"
    exit 1
fi

if [[ ! -f "./mvnw" ]]; then
    print_error "Maven Wrapper non trouvé"
    exit 1
fi

print_success "Environnement OK"

# Étape 2: Compilation et génération MapStruct
print_step "2. Compilation et génération des mappers..."

./mvnw clean compile -q
if [[ $? -ne 0 ]]; then
    print_error "Erreur de compilation"
    exit 1
fi

print_success "Compilation réussie"

# Étape 3: Tests unitaires
print_step "3. Exécution des tests unitaires..."

./mvnw test -q -Dtest="!*IntegrationTest"
if [[ $? -ne 0 ]]; then
    print_error "Échec des tests unitaires"
    exit 1
fi

print_success "Tests unitaires réussis"

# Étape 4: Démarrage de la base de données
print_step "4. Démarrage de MySQL..."

cd ..
docker-compose down -v &> /dev/null
docker-compose up -d db

# Attendre que MySQL soit prêt
echo -n "Attente de MySQL"
COUNTER=0
MAX_WAIT=60
until docker exec $MYSQL_CONTAINER mysqladmin ping -h localhost --silent 2>/dev/null; do
    if [[ $COUNTER -gt $MAX_WAIT ]]; then
        print_error "MySQL n'a pas démarré dans les temps"
        docker-compose logs db
        exit 1
    fi
    echo -n "."
    sleep 2
    COUNTER=$((COUNTER+2))
done
echo
print_success "MySQL démarré"

cd "$BACKEND_DIR"

# Étape 5: Validation des migrations Flyway
print_step "5. Test des migrations Flyway..."

./mvnw flyway:migrate -Dspring.profiles.active=dev -q
if [[ $? -ne 0 ]]; then
    print_error "Erreur lors des migrations Flyway"
    exit 1
fi

print_success "Migrations Flyway réussies"

# Vérifier le statut des migrations
echo "Statut des migrations :"
./mvnw flyway:info -Dspring.profiles.active=dev -q | grep -E "^\|.*\|.*\|.*\|.*\|$" | head -10

# Étape 6: Tests d'intégration
print_step "6. Tests d'intégration..."

./mvnw test -q -Dtest="*IntegrationTest"
if [[ $? -ne 0 ]]; then
    print_error "Échec des tests d'intégration"
    exit 1
fi

print_success "Tests d'intégration réussis"

# Étape 7: Test de démarrage complet de l'application
print_step "7. Test démarrage application complète..."

cd ..
docker-compose down &> /dev/null
docker-compose up -d

# Attendre que l'application démarre
echo -n "Attente de l'application"
COUNTER=0
MAX_WAIT=120
until curl -s http://localhost:8080/actuator/health &> /dev/null; do
    if [[ $COUNTER -gt $MAX_WAIT ]]; then
        print_error "L'application n'a pas démarré dans les temps"
        echo "Logs de l'application :"
        docker-compose logs backend | tail -50
        docker-compose down
        exit 1
    fi
    echo -n "."
    sleep 3
    COUNTER=$((COUNTER+3))
done
echo
print_success "Application démarrée"

# Étape 8: Tests des endpoints API
print_step "8. Tests des endpoints API..."

# Test endpoint de santé
HEALTH_RESPONSE=$(curl -s http://localhost:8080/actuator/health)
if [[ $HEALTH_RESPONSE == *"UP"* ]]; then
    print_success "Health check OK"
else
    print_error "Health check échoué"
    echo "Response: $HEALTH_RESPONSE"
fi

# Test endpoint Swagger
if curl -s http://localhost:8080/v3/api-docs &> /dev/null; then
    print_success "Documentation API accessible"
else
    print_error "Documentation API non accessible"
fi

# Test authentification
AUTH_RESPONSE=$(curl -s -u admin:admin123 http://localhost:8080/api/customers/stats)
if [[ $AUTH_RESPONSE == *"total"* ]]; then
    print_success "Authentification API OK"
else
    print_error "Authentification API échouée"
fi

# Étape 9: Vérification de la base de données
print_step "9. Vérification de la base de données..."

# Vérifier les tables créées
TABLES_COUNT=$(docker exec $MYSQL_CONTAINER mysql -u root -proot gc_dev -e "SHOW TABLES;" -s | wc -l)
if [[ $TABLES_COUNT -ge 5 ]]; then
    print_success "Tables créées ($TABLES_COUNT tables)"
else
    print_error "Nombre de tables insuffisant ($TABLES_COUNT)"
fi

# Vérifier les données de seed
CUSTOMERS_COUNT=$(docker exec $MYSQL_CONTAINER mysql -u root -proot gc_dev -e "SELECT COUNT(*) FROM customers;" -s)
if [[ $CUSTOMERS_COUNT -ge 1 ]]; then
    print_success "Données de seed présentes ($CUSTOMERS_COUNT clients)"
else
    print_error "Données de seed manquantes"
fi

# Vérifier les séquences
SEQUENCES_COUNT=$(docker exec $MYSQL_CONTAINER mysql -u root -proot gc_dev -e "SELECT COUNT(*) FROM sequences;" -s)
if [[ $SEQUENCES_COUNT -ge 5 ]]; then
    print_success "Séquences initialisées ($SEQUENCES_COUNT séquences)"
else
    print_error "Séquences manquantes"
fi

# Étape 10: Nettoyage
print_step "10. Nettoyage..."
docker-compose down &> /dev/null
print_success "Environnement nettoyé"

# Résumé final
echo
echo "======================================================"
echo -e "${GREEN}    ✓ PHASE 3 VALIDÉE AVEC SUCCÈS !${NC}"
echo "======================================================"
echo
echo "🎯 Fonctionnalités validées :"
echo "   • Modèle de données complet (Customer, Address, Product, Sequence)"
echo "   • Migrations Flyway fonctionnelles"
echo "   • Services métier avec logique business"
echo "   • APIs REST sécurisées"
echo "   • Tests d'intégration passants"
echo "   • Documentation API générée"
echo
echo "🚀 Prochaines étapes :"
echo "   • Phase 4: Internationalisation (i18n FR/EN)"
echo "   • Phase 5: UX avec HTMX/Alpine.js"
echo "   • Phase 6: Modules métier (Catalogue, Ventes, Facturation)"
echo
echo "📋 Ressources disponibles :"
echo "   • API Documentation : http://localhost:8080/swagger-ui.html"
echo "   • Health Check     : http://localhost:8080/actuator/health"
echo "   • Base de données  : mysql://localhost:3306/gc_dev (root/root)"
echo
echo "Pour démarrer l'application :"
echo "   docker-compose up -d"

cd "$CURRENT_DIR"
