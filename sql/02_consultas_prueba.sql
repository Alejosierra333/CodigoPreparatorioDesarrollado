USE airport_ops;
SELECT COUNT(*) vuelos FROM vuelos;
SELECT COUNT(*) pasajeros FROM pasajeros;
SELECT COUNT(*) embarques FROM embarques;
SELECT codigo,origen,destino,fecha_programada,estado,puerta FROM vuelos ORDER BY fecha_programada LIMIT 30;
