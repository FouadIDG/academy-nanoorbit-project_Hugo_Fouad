#!/bin/bash

set -Eeuo pipefail

echo "BOOTSTRAP: reinitialisation du schema NO_ADMIN..."

sqlplus -s "NO_ADMIN/Admin_NO_2026@//localhost:1521/FREEPDB1" <<'SQL'
WHENEVER SQLERROR EXIT SQL.SQLCODE
SET DEFINE OFF
SET SERVEROUTPUT ON

@/workspace/sql/01-Phase2_DDL.sql
@/workspace/sql/02-Phase2_DML.sql
@/workspace/sql/03-Phase2_Triggers.sql
@/workspace/sql/07-Phase3_pkg_nanoOrbit_SPEC.sql
@/workspace/sql/06-Phase3_pkg_nanoOrbit_BODY.sql
@/workspace/sql/09-Phase4_Exploitation_Avancee.sql
@/workspace/sql/04-Phase2_Controle.sql

exit
SQL

echo "BOOTSTRAP: schema NO_ADMIN pret."
