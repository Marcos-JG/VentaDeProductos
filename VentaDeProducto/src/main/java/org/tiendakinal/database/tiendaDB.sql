drop database if exists tiendaDB;
create database tiendaDB;
use tiendaDB;

create table Usuarios (
	idUsuario int auto_increment,
	nombreUsuario varchar(64) not null,
	correoUsuario varchar(128) not null,
	contraseñaUsuario varchar(128) not null,
    tipo enum('Empleado', 'Admin'),
    constraint pk_usuarios primary key (idUsuario)
);

create table Productos(
	idProducto int auto_increment,
	nombreProducto varchar(64) not null,
    marcaProducto varchar(64) not null,
	precioProducto decimal(10,2) not null,
	stockProducto int not null,
    codigoBarras varchar(256),
    constraint pk_productos primary key (idProducto) 
);

create table Compras(
	idCompra int auto_increment,
    estadoCompra enum('Pendiente','Completada','Cancelada'),
    estadoPago enum('Pendiente', 'Pagado')default'Pendiente',
    fechaCompra datetime default now(),
    constraint pk_compras primary key (idCompra)
);

create table DetalleCompras(
	idCompra int not null,
    idProducto int not null,
    cantidad int not null,
    subtotal decimal(10,2) not null,
    constraint pk_detallecompras primary key (idCompra, idProducto),
    constraint fk_detalle_compras_compras foreign key (idCompra)
		references Compras(idCompra) on delete cascade,
    constraint fk_detalle_compras_productos foreign key (idProducto)
		references Productos(idProducto) on delete cascade
);

create table Pagos(
	idPago int auto_increment,
    fecha datetime default now(),
	metodoPago enum('Efectivo','Tarjeta'),
	cantidad  decimal(10,2) not null,
    idCompra int not null,
    constraint pk_pagos primary key (idPago),
    constraint fk_pagos_compras foreign key (idCompra) 
		references Compras (idCompra)
);

create table Facturas(
	idFactura int auto_increment,
    fecha datetime default now(),
    total decimal(10,2) not null,
    metodoPago enum('Efectivo','Tarjeta'),
    idEmpleado int not null,
    idCompra int not null,
    idPago int not null,
    constraint pk_facturas primary key (idFactura),

	constraint fk_facturas_empleados foreign key (idEmpleado)
		references Usuarios(idUsuario) on delete cascade,
	constraint fk_facturas_compras foreign key (idCompra)
		references Compras(idCompra) on delete cascade,
	constraint fk_facturas_pagos foreign key (idPago)
		references Pagos(idPago) on delete cascade
);

create table Cliente(
	idCliente int auto_increment,
	nombreCliente varchar (128) default 'Consumidor Final',
    NIT varchar(128) default 'Consumidor Final',
    idCompra int,
    idFactura int,
    constraint pk_clientes primary key (idCliente), 
    constraint fk_facturas_cliente foreign key (idCompra)
		references Compras(idCompra) on delete cascade,
	constraint fk_facturas2_cliente foreign key (idFactura)
		references Facturas(idFactura) on delete cascade
);

-- -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-- CRUD CLIENTE

-- CREATE:
DELIMITER $$ 
create procedure sp_AgregarCliente(
		in p_nombreCliente varchar(200),
		in p_NIT varchar(200),
		in p_idCompra int,
        in p_idFactura int)
	begin
		insert into CLiente(nombreCliente, NIT,idCompra,idFactura)
		values(p_nombreCliente, p_NIT, p_idCompra,p_idFactura);
	end;
$$
DELIMITER ;

DELIMITER $$ 
create procedure sp_AgregarCliente2(
		in p_idCompra int,
        in p_idFactura int)
	begin
		insert into CLiente(idCompra,idFactura)
		values(p_idCompra,p_idFactura);
	end;
$$
DELIMITER ;

-- READ:
DELIMITER $$
create procedure sp_ListarClientes()
    begin
		select 
        idCliente as ID,
        nombreCliente as CLIENTE,
        NIT as NIT,
        idCompra as COMPRA,
        idFactura as FACTURA
        from Cliente;
    end;
$$
DELIMITER ;
call sp_ListarClientes();

-- UPDATE
DELIMITER $$
create procedure sp_ActualizarCliente(
		in p_idCliente int,
		in p_nombreCliente varchar(200),
		in p_NIT varchar(200),
		in p_idCompra int,
        in p_idFactura int)
	begin
		update Cliente
			set
				nombreCliente = p_nombreCliente,
				NIT = p_NIT,
				idCompra = p_idCompra,
                idFactura = p_idFactura
            where 
				p_idCliente = idCliente;
		
	end;
$$
DELIMITER ;

-- DELETE
DELIMITER $$
create procedure sp_EliminarCliente(in p_idCliente int)
    begin
		delete 
        from Cliente
			where idCliente = p_idClientes;
    end 
$$
DELIMITER ;

-- CRUD USUARIOS
-- CREATE
DELIMITER $$ 
create procedure sp_AgregarUsuario(
		in p_nombreUsuario varchar(200),
		in p_correoUsuario varchar(200),
		in p_contraseñaUsuario varchar(200),
		in p_tipo enum ('Empleado','Admin'))
	begin
		insert into Usuarios(nombreUsuario, correoUsuario, contraseñaUsuario, tipo)
		values(p_nombreUsuario, p_correoUsuario,p_contraseñaUsuario, p_tipo);
	end;
$$
DELIMITER ;

-- READ
DELIMITER $$
create procedure sp_ListarUsuario()
    begin
		select 
        idUsuario as ID,
        nombreUsuario as USUARIO,
        correoUsuario as CORREO,
        contraseñaUsuario as CONTRASEÑA,
        tipo as TIPO 
        from Usuarios;
    end;
$$
DELIMITER ;
call sp_ListarUsuario();

-- UPDATE
DELIMITER $$
create procedure sp_ActualizarUsuario(
		in p_idUsuario int,
		in p_nombreUsuario varchar(200),
		in p_correoUsuario varchar(200),
		in p_contraseñaUsuario varchar(200),
		in p_tipo enum ('Empleado','Admin'))
	begin
		update Usuarios
			set
				nombreUsuario = p_nombreUsuario,
				correoUsuario = p_correoUsuario,
				contraseñaUsuario = p_contraseñaUsuario,
				tipo = p_tipo
            where 
				p_idUsuario = idUsuario;
		
	end;
$$
DELIMITER ;
-- call sp_ActualizarUsuario(1,'Kevin Kinalito','kk@gmail.com','2024001','Admin');


-- DELETE
DELIMITER $$
create procedure sp_EliminarUsuario(in p_idUsuario int)
    begin
		delete 
        from Usuarios
			where idUsuario = p_idUsuario;
    end 
$$
DELIMITER ;
-- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-- CRUD PRODUCTOS

-- CREATE
DELIMITER $$
create procedure sp_agregarProducto(
    in p_nombreProducto varchar (200),
    in p_marcaProducto varchar(64),
    in p_precioProducto decimal(10,2),
    in p_stockProducto int,
    in p_codigoBarras varchar(200))
	begin
		insert into Productos(
        nombreProducto, marcaProducto, precioProducto,stockProducto,codigoBarras)
        values(p_nombreProducto, p_marcaProducto, p_precioProducto,p_stockProducto,p_codigoBarras);
    end
$$
DELIMITER ;

-- READ
DELIMITER $$
create procedure sp_ListarProductos()
    begin
		select 
        idProducto as ID,
        nombreProducto as PRODUCTO,
        precioProducto as PRECIO,
        stockProducto as STOCK,
        codigoBarras as 'CODIGO DE BARRAS'
        from Productos;
    end;
$$
DELIMITER ;
-- call sp_ListarProductos();

-- UPDATE
DELIMITER $$
create procedure sp_ActualizarProducto(
		in p_idProducto int,
		in p_nombreProducto varchar(200),
		in p_precioProducto decimal(10,2),
		in p_stockProducto int,
		in p_codigoBarras varchar (200))
	begin
		update Productos
			set
				nombreProducto = p_nombreProducto,
				precioProducto = p_precioProducto,
				stockProducto = p_stockProducto,
				codigoBarras = p_codigoBarras
            where 
				p_idProducto = idProducto;
		
	end;
$$
DELIMITER ;
-- call sp_ActualizarProducto(1,'Señorial',2.0,10,'A-0020-Z');

-- DELETE
DELIMITER $$
create procedure sp_EliminarProducto(in p_idProducto int)
    begin
		delete 
        from Productos
			where idProducto = p_idProducto;
    end 
$$
DELIMITER ;
-- call sp_EliminarProducto(1);
-- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

-- CRUD COMPRAS

-- CREATE
DELIMITER $$
create procedure sp_agregarCompra(
    in p_estadoCompra enum ('Pendiente','Completada','Cancelada'),
    in p_estadoPago enum ('Pendiente','Pagado'))
	begin
		insert into Compras(
        estadoCompra,estadoPago)
        values(p_estadoCompra,p_estadoPago);
    end
$$
DELIMITER ;


-- READ
DELIMITER $$
CREATE PROCEDURE sp_ListarCompras()
BEGIN
    SELECT 
        c.idCompra AS COMPRA,
        (SELECT SUM(subtotal) FROM DetalleCompras WHERE idCompra = c.idCompra) * 1.12 AS TOTAL,
        c.estadoCompra AS ESTADO_COMPRA,
        c.estadoPago AS ESTADO_PAGO,
        c.fechaCompra AS FECHA
    FROM Compras c;
END$$
DELIMITER ;
call sp_ListarCompras();		

-- UPDATE
DELIMITER $$
create procedure sp_ActualizarCompras(
		in p_idCompra int,
		in p_estadoCompra enum('Pendiente','Completada','Cancelada'),
		in p_estadoPago enum('Pendiente', 'Pagado'))
	begin
		update Compras
			set
                estadoCompra = p_estadoCompra,
				estadoPago = p_estadoPago
            where 
				p_idCompra = idCompra;
		
	end;
$$
DELIMITER ;
-- call sp_ActualizarCompras(2,2,'Completada','Pendiente');

-- DELETE
DELIMITER $$
create procedure sp_EliminarCompras(in p_idCompras int)
    begin
		delete 
        from Compras
			where idCompra = p_idCompras;
    end 
$$
DELIMITER ;
-- call sp_EliminarCompras(1);
-- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-- CRUD DETALLECOMPRAS

-- CREATE
DELIMITER $$
create procedure sp_agregarDetalleCompra(
	in p_idCompra int,
    in p_idProducto int,
    in p_cantidad int)
	begin
		insert into DetalleCompras(
        idCompra,idProducto,cantidad)
        values(p_idCompra,p_idProducto,p_cantidad);
    end
$$
DELIMITER ;

-- READ
DELIMITER $$
create procedure sp_ListarDetalleCompras()
    begin
		select 
        idCompra as COMPRA,
        idProducto as PRODUCTO,
        cantidad as CANTIDAD,
        subtotal as SUBTOTAL
        from DetalleCompras;
    end;
$$
DELIMITER ;
call sp_ListarDetalleCompras();

-- UPDATE
DELIMITER $$
create procedure sp_ActualizarDetalleCompras(
		in p_idCompra int,
        in p_idProducto int,
		in p_cantidad int,
		in p_subtotal decimal(10,2))
	begin
		update DetalleCompras
			set
                cantidad = p_cantidad,
				subtotal = p_subtotal
            where 
				 idCompra = p_idCompra and idProducto = p_idProducto;
		
	end;
$$
DELIMITER ;
-- call sp_ActualizarDetalleCompras(2,2,2,3.0);

-- DELETE
DELIMITER $$
create procedure sp_EliminarDetalleCompras(in p_idCompras int)
    begin
		delete 
        from DetalleCompras
			where idCompra = p_idCompras;
    end 
$$
DELIMITER ;
-- call sp_EliminarDetalleCompras(2);

 DELIMITER $$
CREATE PROCEDURE sp_ReporteVentas(
    IN p_fechaInicio DATE,
    IN p_fechaFin DATE)
BEGIN
    SELECT 
        c.idCompra,
        u.nombreUsuario AS cliente,
        COUNT(dc.idProducto) AS productos,
        SUM(dc.subtotal) AS total,
        c.fechaCompra
    FROM Compras c

    JOIN DetalleCompras dc ON c.idCompra = dc.idCompra
    WHERE c.fechaCompra BETWEEN p_fechaInicio AND p_fechaFin
    AND c.estadoCompra = 'Completada'
    GROUP BY c.idCompra;
END$$
DELIMITER ;
 
 -- -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-- CRUD PAGOS

-- CREATE
DELIMITER $$
create procedure sp_AgregarPagos(
    in p_metodoPago enum('Efectivo','Tarjeta','Transferencia'),
    in p_cantidad decimal(10,2),
    in p_idCompra int,
    out p_idPago int)
begin
    insert into Pagos(metodoPago, cantidad, idCompra)
    values(p_metodoPago, p_cantidad, p_idCompra);
    
    set p_idPago = LAST_INSERT_ID();
end$$
DELIMITER ;

-- READ
DELIMITER $$
create procedure sp_ListarPagos()
    begin
		select 
        idPago as PAGO,
        fecha as FECHA,
        cantidad as CANTIDAD,
        idCompra as COMPRA
        from Pagos;
    end;
$$
DELIMITER ;
call sp_ListarPagos();

-- UPDATE
DELIMITER $$
create procedure sp_ActualizarPagos(
		in p_idPago int,
		in p_cantidad decimal(10,2),
		in p_idCompra int)
	begin
		if not exists 
        (select 1 from Compras where idCompra = p_idCompra) 
        then 
        signal sqlstate '45000' 
        set message_text = 'La compra no existe';
        else
		update Pagos
			set
                cantidad = p_cantidad,
				idCompra = p_idCompra
            where 
				idPago = p_idPago;
		end if;
	end;
$$
DELIMITER ;
-- call sp_ActualizarPagos(2,1055,2);

-- DELETE
DELIMITER $$
create procedure sp_EliminarPagos(in p_idPagos int)
    begin
		delete 
        from Pagos
			where idPago = p_idPagos;
    end 
$$
DELIMITER ;
-- call sp_EliminarPagos(1);
 
 -- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
-- CRUD FACTURAS

-- CREATE
DELIMITER $$
create procedure sp_AgregarFactura(
    in p_metodoPago enum('Efectivo','Tarjeta'),
    in p_idEmpleado int,
    in p_idCompra int,
    in p_idPago int)
	begin
		insert into Facturas(
        metodoPago,idEmpleado,idCompra,idPago)
        values(p_metodoPago,p_idEmpleado,p_idCompra,p_idPago);
    end
$$
DELIMITER ;

-- READ
DELIMITER $$
create procedure sp_ListarFactura()
    begin
		select 
        idFactura as FACTURA,
        fecha as FECHA,
        total as TOTAL,
        metodoPago as METODO_PAGO,
        idEmpleado as EMPLEADO,
        idCompra as COMPRA,
        idPago as PAGO
        from Facturas;
    end;
$$
DELIMITER ;
call sp_ListarFactura();



-- UPDATE
DELIMITER $$
create procedure sp_ActualizarFactura(
		in p_idFactura int,
		in p_total decimal(10,2),
		in p_metodoPago enum('Efectivo','Tarjeta','Transferencia'),
		in p_idEmpleado int,
		in p_idCompra int,
		in p_idPago int)
	begin
		update Facturas
			set
                total = p_total,
				metodoPago = p_metodoPago,
 
                idEmpleado = p_idEmpleado,
                idCompra = p_idCompra,
                idPago = p_idPago
            where 
				p_idFactura = idFactura;
	end;
$$
DELIMITER ;
-- call sp_ActualizarFactura(2,1000.00,'Tarjeta',2,2,2,2);

-- DELETE
DELIMITER $$
create procedure sp_EliminarFacturas(in p_idFactura int)
    begin
		delete 
        from Facturas
			where idFactura = p_idFactura;
    end 
$$
DELIMITER ;
-- call sp_EliminarFacturas(1); 
 -- ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
 delimiter $$
create trigger tr_CalcularSubTotal_Before_Insert
before insert on DetalleCompras
for each row
begin
    declare precio decimal(10,2);
    
    select precioProducto into precio from Productos where idProducto = new.idProducto;
    
    set new.subtotal = precio * new.cantidad;
end$$
delimiter ;

delimiter $$
create trigger tr_CalcularTotal_Before_Insert
before insert on Facturas
for each row
begin
    declare total_compra decimal(10,2);
    
    select sum(subtotal) into total_compra 
    from detallecompras 
    where idCompra = new.idCompra;
    
    set new.total = total_compra;
end$$
delimiter ;

call sp_AgregarUsuario("Roger","rv","rv","Empleado");
call sp_AgregarUsuario("Marcos","mg","mg","Empleado");
call sp_AgregarUsuario("Iosef","is","is","Empleado");
call sp_ListarUsuario();

call sp_AgregarProducto('Chapulines','Chicote', 250.00, 30, '10001');
call sp_AgregarProducto('caites','Chicote','100.00',40,'10002');
call sp_AgregarProducto('tennis','Chicote','300.00',50,'10003');
CALL sp_AgregarProducto('Ultraboost 22', 'Adidas', '3299.00', 50, '10004');
CALL sp_AgregarProducto('Classic Leather', 'Reebok', '1899.00', 50, '10005');
CALL sp_AgregarProducto('Chuck Taylor All Star', 'Converse', '1499.00', 50, '10006');
CALL sp_AgregarProducto('Old Skool', 'Vans', '1599.00', 50, '10007');
CALL sp_AgregarProducto('Air Jordan 1 Mid', 'Jordan', '3599.00', 50, '10008');
CALL sp_AgregarProducto('Gel-Kayano 30', 'Asics', '3899.00', 50, '10009');
CALL sp_AgregarProducto('RS-X Efekt', 'Puma', '2799.00', 50, '10010');
CALL sp_AgregarProducto('FuelCell Propel v4', 'New Balance', '3199.00', 50, '10011');
CALL sp_AgregarProducto('Cloudswift 3', 'On Running', '4299.00', 50, '10012');

CALL sp_AgregarProducto('Blazer Mid 77', 'Nike', '2499.00', 50, '10013');
CALL sp_AgregarProducto('Forum Low', 'Adidas', '1999.00', 50, '10014');
CALL sp_AgregarProducto('Zig Kinetica 2.5', 'Reebok', '2699.00', 50, '10015');
CALL sp_AgregarProducto('Run Star Hike', 'Converse', '2799.00', 50, '10016');
CALL sp_AgregarProducto('Sk8-Hi', 'Vans', '1699.00', 50, '10017');
CALL sp_AgregarProducto('Air Force 1', 'Nike', '2599.00', 50, '10018');
CALL sp_AgregarProducto('Pegasus 41', 'Nike', '3199.00', 50, '10019');
CALL sp_AgregarProducto('Suede Classic XXI', 'Puma', '1899.00', 50, '10020');
CALL sp_AgregarProducto('574 Core', 'New Balance', '2199.00', 50, '10021');
CALL sp_AgregarProducto('Cloud 5', 'On Running', '3899.00', 50, '10022');

CALL sp_AgregarProducto('ZoomX Invincible Run', 'Nike', '4299.00', 50, '10023');
CALL sp_AgregarProducto('Yeezy Boost 350 V2', 'Adidas', '5999.00', 50, '10024');
CALL sp_AgregarProducto('Club C 85', 'Reebok', '1699.00', 50, '10025');
CALL sp_AgregarProducto('Jack Purcell', 'Converse', '1999.00', 50, '10026');
CALL sp_AgregarProducto('Authentic', 'Vans', '1399.00', 50, '10027');
CALL sp_AgregarProducto('React Infinity Run', 'Nike', '3799.00', 50, '10028');
CALL sp_AgregarProducto('X_PLRBOOST', 'Adidas', '3099.00', 50, '10029');
CALL sp_AgregarProducto('Revolution 6', 'Nike', '1599.00', 50, '10030');
CALL sp_AgregarProducto('NMD_R1', 'Adidas', '3399.00', 50, '10031');
CALL sp_AgregarProducto('Nano X3', 'Reebok', '3499.00', 50, '10032');

CALL sp_AgregarProducto('React Pegasus Trail 4', 'Nike', '3699.00', 50, '10033');
CALL sp_AgregarProducto('ZX 22 Boost', 'Adidas', '2899.00', 50, '10034');
CALL sp_AgregarProducto('Waffle One', 'Nike', '2299.00', 50, '10035');
CALL sp_AgregarProducto('Trek Chuck Taylor', 'Converse', '2699.00', 50, '10036');
CALL sp_AgregarProducto('Slip-On', 'Vans', '1299.00', 50, '10037');
CALL sp_AgregarProducto('RS-TRCK', 'Puma', '2999.00', 50, '10038');
CALL sp_AgregarProducto('FuelCell SuperComp Elite v4', 'New Balance', '5199.00', 50, '10039');
CALL sp_AgregarProducto('Cloudmonster 2', 'On Running', '5199.00', 50, '10040');
CALL sp_AgregarProducto('Air Zoom Structure 25', 'Nike', '3399.00', 50, '10041');
CALL sp_AgregarProducto('Terrex Swift R3 GTX', 'Adidas', '4499.00', 50, '10042');

CALL sp_AgregarProducto('Cl Legacy AZ', 'Reebok', '1999.00', 50, '10043');
CALL sp_AgregarProducto('Air Huarache', 'Nike', '2999.00', 50, '10044');
CALL sp_AgregarProducto('Campus 00s', 'Adidas', '2899.00', 50, '10045');
CALL sp_AgregarProducto('Zoom Vomero 5', 'Nike', '4099.00', 50, '10046');
CALL sp_AgregarProducto('Future Rider', 'Puma', '2099.00', 50, '10047');
CALL sp_AgregarProducto('Made in USA 990v6', 'New Balance', '6399.00', 50, '10048');
call sp_ListarProductos();

call sp_AgregarCompra('Pendiente','Pagado');
call sp_AgregarCompra('Completada','Pagado');
call sp_ListarCompras();

call sp_agregarDetalleCompra(1,1,3);
call sp_agregarDetalleCompra(1,2,3);
call sp_ListarDetalleCompras();