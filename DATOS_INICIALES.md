# Datos Iniciales del Sistema de Control de Asistencia

## Resumen de Usuarios Creados

### Administradores (2)
1. **admin** / admin123
   - Nombre: Carlos Mendoza
   - Código: ADM-001
   - Email: carlos.mendoza@agrocyt.com
   - Cargo: Gerente General
   - Salario: S/ 6,000.00
   - Estado: ACTIVO

2. **subadmin** / subadmin123
   - Nombre: Roberto Silva
   - Código: ADM-002
   - Email: roberto.silva@agrocyt.com
   - Cargo: Subgerente de Operaciones
   - Salario: S/ 4,500.00
   - Estado: ACTIVO

### Especialista RRHH (1)
3. **rrhh** / rrhh123
   - Nombre: Ana Gómez
   - Código: RRHH-001
   - Email: ana.gomez@agrocyt.com
   - Cargo: Especialista RRHH
   - Salario: S/ 3,500.00
   - Estado: ACTIVO

### Supervisores (2)
4. **supervisor1** / sup123
   - Nombre: Miguel Torres
   - Código: SUP-001
   - Email: miguel.torres@agrocyt.com
   - Cargo: Supervisor de Campo
   - Salario: S/ 2,800.00
   - Estado: ACTIVO

5. **supervisor2** / sup123
   - Nombre: Elena Vargas
   - Código: SUP-002
   - Email: elena.vargas@agrocyt.com
   - Cargo: Supervisor de Mantenimiento
   - Salario: S/ 2,800.00
   - Estado: ACTIVO

### Trabajadores (6)
6. **maria** / pass123
   - Nombre: María López
   - Código: EMP-001
   - Email: maria.lopez@agrocyt.com
   - Cargo: Cosechador
   - Salario: S/ 1,200.00
   - Estado: ACTIVO

7. **juan** / pass123
   - Nombre: Juan Pérez
   - Código: EMP-002
   - Email: juan.perez@agrocyt.com
   - Cargo: Cosechador
   - Salario: S/ 1,200.00
   - Estado: ACTIVO

8. **pedro** / pass123
   - Nombre: Pedro Ramírez
   - Código: EMP-003
   - Email: pedro.ramirez@agrocyt.com
   - Cargo: Operador de Maquinaria
   - Salario: S/ 1,500.00
   - Estado: ACTIVO

9. **lucia** / pass123
   - Nombre: Lucía Fernández
   - Código: EMP-004
   - Email: lucia.fernandez@agrocyt.com
   - Cargo: Técnico de Mantenimiento
   - Salario: S/ 1,400.00
   - Estado: ACTIVO

10. **carmen** / pass123
    - Nombre: Carmen Ruiz
    - Código: EMP-005
    - Email: carmen.ruiz@agrocyt.com
    - Cargo: Asistente Administrativo
    - Salario: S/ 1,300.00
    - Estado: ACTIVO

11. **diego** / pass123
    - Nombre: Diego Morales
    - Código: EMP-006
    - Email: diego.morales@agrocyt.com
    - Cargo: Contador
    - Salario: S/ 2,000.00
    - Estado: ACTIVO

## Horarios de Trabajo Creados

1. **Turno Cosecha Mañana** (7:00 - 15:00)
   - Asignado a: María López, Juan Pérez
   - Tolerancia: 15 minutos

2. **Turno Operación Tarde** (15:00 - 23:00)
   - Asignado a: Pedro Ramírez
   - Tolerancia: 15 minutos

3. **Turno Administrativo** (8:00 - 17:00)
   - Asignado a: Carmen Ruiz, Diego Morales, Miguel Torres, Elena Vargas
   - Tolerancia: 10 minutos

4. **Turno Mantenimiento** (6:00 - 14:00)
   - Asignado a: Lucía Fernández
   - Tolerancia: 15 minutos

## Tipos de Solicitudes Disponibles

1. Permiso Personal
2. Solicitud de Vacaciones
3. Solicitud de Constancia de Trabajo
4. Solicitud de Permiso Médico
5. Solicitud de Cambio de Turno
6. Solicitud de Capacitación

## Solicitudes de Ejemplo Creadas

1. **María López** - Permiso Personal (PENDIENTE)
   - Motivo: Cita médica de emergencia
   - Fecha: Próximos 2 días

2. **Juan Pérez** - Vacaciones (APROBADA)
   - Motivo: Vacaciones familiares
   - Período: 10-17 días desde hoy

3. **Pedro Ramírez** - Constancia de Trabajo (APROBADA)
   - Motivo: Para trámite bancario
   - Fecha: Ayer

4. **Lucía Fernández** - Permiso Médico (PENDIENTE)
   - Motivo: Cirugía programada
   - Período: 5-7 días desde hoy

5. **Carmen Ruiz** - Cambio de Turno (RECHAZADA)
   - Motivo: Por motivos familiares
   - Fecha: Próximos 3 días

6. **Diego Morales** - Vacaciones (PENDIENTE)
   - Motivo: Vacaciones de fin de año
   - Período: 20-30 días desde hoy

## Registros de Asistencia de Ejemplo

### Día Actual
- **María López**: Entrada 7:05, Salida 15:30 (30 min extra)
- **Juan Pérez**: Entrada 7:10 (sin salida registrada)
- **Pedro Ramírez**: Entrada 14:55, Salida 22:45
- **Lucía Fernández**: Entrada 6:05, Salida 14:10
- **Carmen Ruiz**: Entrada 8:05, Salida 17:15 (15 min extra)
- **Diego Morales**: Entrada 7:55, Salida 17:05

### Día Anterior
- **María López**: Entrada 7:00, Salida 15:00
- **Juan Pérez**: Entrada 7:15, Salida 15:10

## Funcionalidades Implementadas

### Control de Estado de Usuarios
- Campo `isEnabled` en la entidad User
- Validación en UserDetailsImpl para prohibir acceso a usuarios suspendidos
- Endpoints para activar/desactivar usuarios:
  - `POST /api/employee/{userId}/activate`
  - `POST /api/employee/{userId}/deactivate`
  - `GET /api/employee/{userId}/status`

### Seguridad
- Todos los usuarios tienen contraseñas encriptadas
- Roles y permisos configurados correctamente
- Validación de estado de usuario en autenticación

## Notas Importantes

- Todos los usuarios están creados con estado ACTIVO por defecto
- Las contraseñas son simples para facilitar las pruebas
- Los datos se cargan automáticamente al iniciar la aplicación
- Los registros de asistencia incluyen diferentes escenarios (horas extra, empleados sin salida, etc.)
- Las solicitudes tienen diferentes estados para probar el flujo completo

