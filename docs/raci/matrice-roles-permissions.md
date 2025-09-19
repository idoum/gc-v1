<!-- 
  @path docs/raci/matrice-roles-permissions.md
  @description Matrice des rôles et permissions pour la v1
-->
# Matrice Rôles ↔ Permissions v1

## Définition des Rôles

| Rôle     | Description                                      | Utilisateurs types     |
|----------|--------------------------------------------------|------------------------|
| ADMIN    | Administrateur système - Tous droits            | Administrateur IT      |
| MANAGER  | Gestionnaire - Gestion opérationnelle          | Chef des ventes       |
| USER     | Utilisateur standard - Consultation et saisie  | Commercial, Assistant  |

## Matrice des Permissions

| Module      | Action  | ADMIN | MANAGER | USER | Description                           |
|-------------|---------|-------|---------|------|---------------------------------------|
| **SECURITY** |         |       |         |      |                                       |
| Users       | CREATE  | ✓     | ✗       | ✗    | Créer un utilisateur                  |
| Users       | READ    | ✓     | ✓       | ✗    | Consulter les utilisateurs            |
| Users       | UPDATE  | ✓     | ✗       | ✗    | Modifier un utilisateur               |
| Users       | DELETE  | ✓     | ✗       | ✗    | Supprimer un utilisateur              |
| Roles       | CREATE  | ✓     | ✗       | ✗    | Créer un rôle                         |
| Roles       | READ    | ✓     | ✓       | ✗    | Consulter les rôles                   |
| Roles       | UPDATE  | ✓     | ✗       | ✗    | Modifier un rôle                      |
| Roles       | DELETE  | ✓     | ✗       | ✗    | Supprimer un rôle                     |
| Audit       | READ    | ✓     | ✓       | ✗    | Consulter les logs d'audit            |
| **CATALOGUE** |       |       |         |      |                                       |
| Products    | CREATE  | ✓     | ✓       | ✗    | Créer un produit                      |
| Products    | READ    | ✓     | ✓       | ✓    | Consulter les produits                |
| Products    | UPDATE  | ✓     | ✓       | ✗    | Modifier un produit                   |
| Products    | DELETE  | ✓     | ✓       | ✗    | Supprimer un produit                  |
| Categories  | CREATE  | ✓     | ✓       | ✗    | Créer une catégorie                   |
| Categories  | READ    | ✓     | ✓       | ✓    | Consulter les catégories              |
| Categories  | UPDATE  | ✓     | ✓       | ✗    | Modifier une catégorie                |
| Categories  | DELETE  | ✓     | ✓       | ✗    | Supprimer une catégorie               |
| **CRM**     |         |       |         |      |                                       |
| Customers   | CREATE  | ✓     | ✓       | ✓    | Créer un client                       |
| Customers   | READ    | ✓     | ✓       | ✓    | Consulter les clients                 |
| Customers   | UPDATE  | ✓     | ✓       | ✓    | Modifier un client                    |
| Customers   | DELETE  | ✓     | ✓       | ✗    | Supprimer un client                   |
| **VENTES**  |         |       |         |      |                                       |
| Orders      | CREATE  | ✓     | ✓       | ✓    | Créer une commande                    |
| Orders      | READ    | ✓     | ✓       | ✓    | Consulter les commandes               |
| Orders      | UPDATE  | ✓     | ✓       | ✓    | Modifier une commande                 |
| Orders      | DELETE  | ✓     | ✓       | ✗    | Supprimer une commande                |
| Orders      | VALIDATE| ✓     | ✓       | ✗    | Valider une commande                  |
| **FACTURATION** |     |       |         |      |                                       |
| Invoices    | CREATE  | ✓     | ✓       | ✗    | Créer une facture                     |
| Invoices    | READ    | ✓     | ✓       | ✓    | Consulter les factures                |
| Invoices    | UPDATE  | ✓     | ✓       | ✗    | Modifier une facture                  |
| Invoices    | DELETE  | ✓     | ✗       | ✗    | Supprimer une facture                 |
| Invoices    | EXPORT  | ✓     | ✓       | ✓    | Exporter une facture PDF              |

## Actions Auditées

Toutes les actions suivantes sont enregistrées dans `audit_logs` :

- **Connexion/Déconnexion** : LOGIN, LOGOUT
- **Sécurité** : CREATE_USER, UPDATE_USER, DELETE_USER, ACCESS_DENIED
- **Données métier** : CREATE, UPDATE, DELETE sur tous les modules
- **Exports** : EXPORT_PDF, EXPORT_CSV
- **Erreurs** : FAILED_LOGIN, SYSTEM_ERROR
