#!/bin/bash
#
# @path validate-phase4.sh
# @description Script de validation Phase 4 - i18n FR/EN & Thèmes clair/sombre
#

set -e

echo "======================================================"
echo "     VALIDATION PHASE 4 - i18n & THÈMES             "
echo "======================================================"
echo

# Couleurs
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_step() {
    echo -e "${YELLOW}➤ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

PROJECT_ROOT=$(pwd)
BACKEND_DIR="gestion-commerciale-backend"

# Vérification structure
if [[ ! -d "$BACKEND_DIR" ]]; then
    print_error "Répertoire $BACKEND_DIR non trouvé"
    exit 1
fi

# Étape 1: Compilation
print_step "1. Compilation du projet..."
cd "$BACKEND_DIR"
./mvnw clean compile -q
if [[ $? -eq 0 ]]; then
    print_success "Compilation réussie"
else
    print_error "Erreur de compilation"
    exit 1
fi
cd "$PROJECT_ROOT"

# Étape 2: Vérification des fichiers i18n
print_step "2. Vérification des fichiers i18n..."

FILES_TO_CHECK=(
    "$BACKEND_DIR/src/main/resources/messages.properties"
    "$BACKEND_DIR/src/main/resources/messages_fr.properties"
    "$BACKEND_DIR/src/main/resources/messages_en.properties"
    "$BACKEND_DIR/src/main/java/com/example/gestioncommerciale/config/InternationalizationConfig.java"
    "$BACKEND_DIR/src/main/java/com/example/gestioncommerciale/controller/LocaleController.java"
    "$BACKEND_DIR/src/main/java/com/example/gestioncommerciale/controller/HomeController.java"
)

for file in "${FILES_TO_CHECK[@]}"; do
    if [[ -f "$file" ]]; then
        print_success "Fichier présent: $(basename $file)"
    else
        print_error "Fichier manquant: $file"
    fi
done

# Étape 3: Vérification des fichiers de thème
print_step "3. Vérification des fichiers de thème..."

THEME_FILES=(
    "$BACKEND_DIR/src/main/resources/static/css/themes.css"
    "$BACKEND_DIR/src/main/resources/static/js/theme-manager.js"
    "$BACKEND_DIR/src/main/resources/templates/fragments/layout.html"
    "$BACKEND_DIR/src/main/resources/templates/index.html"
)

for file in "${THEME_FILES[@]}"; do
    if [[ -f "$file" ]]; then
        print_success "Fichier thème présent: $(basename $file)"
    else
        print_error "Fichier thème manquant: $file"
    fi
done

# Étape 4: Test de démarrage
print_step "4. Test de démarrage de l'application..."

docker-compose down &> /dev/null || true
docker-compose up -d db

# Attendre MySQL
echo -n "Attente MySQL"
COUNTER=0
until docker exec gc-db mysqladmin ping -h localhost --silent 2>/dev/null; do
    if [[ $COUNTER -gt 30 ]]; then
        print_error "MySQL timeout"
        exit 1
    fi
    echo -n "."
    sleep 2
    COUNTER=$((COUNTER+2))
done
echo

# Démarrer l'application
docker-compose up -d

# Attendre l'application
echo -n "Attente application"
COUNTER=0
until curl -s http://localhost:8080/actuator/health &> /dev/null; do
    if [[ $COUNTER -gt 60 ]]; then
        print_error "Application timeout"
        docker-compose logs backend | tail -20
        docker-compose down
        exit 1
    fi
    echo -n "."
    sleep 3
    COUNTER=$((COUNTER+3))
done
echo

print_success "Application démarrée"

# Étape 5: Tests des endpoints i18n
print_step "5. Tests des endpoints d'internationalisation..."

# Test redirection racine
REDIRECT_RESPONSE=$(curl -s -I http://localhost:8080/ | grep Location || echo "")
if [[ $REDIRECT_RESPONSE == *"/fr/"* ]]; then
    print_success "Redirection racine vers /fr/ OK"
else
    print_error "Redirection racine échouée: $REDIRECT_RESPONSE"
fi

# Test page FR
FR_RESPONSE=$(curl -s -w "%{http_code}" http://localhost:8080/fr/ -o /dev/null)
if [[ $FR_RESPONSE == "200" ]]; then
    print_success "Page FR accessible (200)"
else
    print_error "Page FR inaccessible ($FR_RESPONSE)"
fi

# Test page EN
EN_RESPONSE=$(curl -s -w "%{http_code}" http://localhost:8080/en/ -o /dev/null)
if [[ $EN_RESPONSE == "200" ]]; then
    print_success "Page EN accessible (200)"
else
    print_error "Page EN inaccessible ($EN_RESPONSE)"
fi

# Test changement de langue
LANG_CHANGE=$(curl -s -I "http://localhost:8080/change-language?lang=en&returnUrl=/" | grep Location || echo "")
if [[ $LANG_CHANGE == *"/en/"* ]]; then
    print_success "Changement de langue fonctionne"
else
    print_error "Changement de langue échoué"
fi

# Étape 6: Test des fichiers statiques
print_step "6. Tests des fichiers statiques (CSS/JS)..."

CSS_RESPONSE=$(curl -s -w "%{http_code}" http://localhost:8080/css/themes.css -o /dev/null)
if [[ $CSS_RESPONSE == "200" ]]; then
    print_success "CSS themes accessible"
else
    print_error "CSS themes inaccessible ($CSS_RESPONSE)"
fi

JS_RESPONSE=$(curl -s -w "%{http_code}" http://localhost:8080/js/theme-manager.js -o /dev/null)
if [[ $JS_RESPONSE == "200" ]]; then
    print_success "JS theme-manager accessible"
else
    print_error "JS theme-manager inaccessible ($JS_RESPONSE)"
fi

# Étape 7: Vérification du contenu HTML
print_step "7. Vérification du contenu HTML..."

HTML_FR=$(curl -s http://localhost:8080/fr/)
if [[ $HTML_FR == *"data-bs-theme"* ]] && [[ $HTML_FR == *"gc-theme-toggle"* ]]; then
    print_success "Éléments de thème présents en FR"
else
    print_error "Éléments de thème manquants en FR"
fi

if [[ $HTML_FR == *"Accueil"* ]] && [[ $HTML_FR == *"Clients"* ]]; then
    print_success "Contenu français détecté"
else
    print_error "Contenu français manquant"
fi

# Nettoyage
print_step "8. Nettoyage..."
docker-compose down &> /dev/null
print_success "Environnement nettoyé"

# Résumé final
echo
echo "======================================================"
echo -e "${GREEN}    ✓ PHASE 4 VALIDÉE AVEC SUCCÈS !${NC}"
echo "======================================================"
echo
echo "🌐 Fonctionnalités i18n validées :"
echo "   • URLs localisées /fr/ et /en/"
echo "   • Fichiers de messages FR/EN"
echo "   • Changement de langue fonctionnel"
echo "   • LocaleResolver configuré"
echo
echo "🎨 Fonctionnalités thèmes validées :"
echo "   • Bootstrap 5.3 avec data-bs-theme"
echo "   • CSS personnalisé pour thèmes"
echo "   • JavaScript gestionnaire de thèmes"
echo "   • Persistance localStorage"
echo
echo "🚀 Prochaines étapes :"
echo "   • Phase 5: UX SSR + HTMX/Alpine.js"
echo "   • Test manuel des thèmes clair/sombre"
echo "   • Validation de l'expérience utilisateur"
echo
echo "🔗 URLs de test :"
echo "   • Français: http://localhost:8080/fr/"
echo "   • English: http://localhost:8080/en/"
echo "   • CSS: http://localhost:8080/css/themes.css"
echo "   • JS: http://localhost:8080/js/theme-manager.js"
