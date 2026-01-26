--
-- PostgreSQL database dump
--

\restrict I1AJ6EDDBza42ddfHN15JdrO9Tp3zuqqj9Qpj5WJFrpoDGVul9pkxVIJOg32bug

-- Dumped from database version 14.20 (Ubuntu 14.20-0ubuntu0.22.04.1)
-- Dumped by pg_dump version 14.20 (Ubuntu 14.20-0ubuntu0.22.04.1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: sig; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA sig;


ALTER SCHEMA sig OWNER TO postgres;

--
-- Name: system; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA system;


ALTER SCHEMA system OWNER TO postgres;

--
-- Name: postgis; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS postgis WITH SCHEMA public;


--
-- Name: EXTENSION postgis; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION postgis IS 'PostGIS geometry and geography spatial types and functions';


--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: bidang; Type: TABLE; Schema: sig; Owner: postgres
--

CREATE TABLE sig.bidang (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    kd_prop character(2) NOT NULL,
    kd_dati2 character(2) NOT NULL,
    kd_kec character(3) NOT NULL,
    kd_kel character(3) NOT NULL,
    kd_blok character(3) NOT NULL,
    no_urut character(4) NOT NULL,
    kd_jns_op character(1) NOT NULL,
    nop character(24),
    geom text NOT NULL,
    created_at timestamp without time zone DEFAULT now() NOT NULL,
    created_by uuid,
    updated_at timestamp without time zone,
    updated_by uuid,
    deleted_at timestamp without time zone,
    deleted_by uuid,
    recover_at timestamp without time zone,
    recover_by uuid,
    is_active boolean DEFAULT true NOT NULL
);


ALTER TABLE sig.bidang OWNER TO postgres;

--
-- Name: blok; Type: TABLE; Schema: sig; Owner: postgres
--

CREATE TABLE sig.blok (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    kd_prop character(2) NOT NULL,
    kd_dati2 character(2) NOT NULL,
    kd_kec character(3) NOT NULL,
    kd_kel character(3) NOT NULL,
    kd_blok character(3) NOT NULL,
    geom text NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    created_by character(36),
    updated_at timestamp without time zone,
    updated_by character(36),
    deleted_at timestamp without time zone,
    deleted_by character(36),
    recover_at timestamp without time zone,
    recover_by character(36),
    is_active boolean DEFAULT true
);


ALTER TABLE sig.blok OWNER TO postgres;

--
-- Name: desa_kepuharjo; Type: TABLE; Schema: system; Owner: postgres
--

CREATE TABLE system.desa_kepuharjo (
    id integer NOT NULL,
    geom public.geometry(MultiPolygon,4326),
    "FID" bigint,
    "Id" integer,
    d_nm_kel character varying(100),
    d_kd_kel character varying(10)
);


ALTER TABLE system.desa_kepuharjo OWNER TO postgres;

--
-- Name: desa_kepuharjo_id_seq; Type: SEQUENCE; Schema: system; Owner: postgres
--

CREATE SEQUENCE system.desa_kepuharjo_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE system.desa_kepuharjo_id_seq OWNER TO postgres;

--
-- Name: desa_kepuharjo_id_seq; Type: SEQUENCE OWNED BY; Schema: system; Owner: postgres
--

ALTER SEQUENCE system.desa_kepuharjo_id_seq OWNED BY system.desa_kepuharjo.id;


--
-- Name: kecamatan; Type: TABLE; Schema: system; Owner: postgres
--

CREATE TABLE system.kecamatan (
    id character(36) DEFAULT public.uuid_generate_v4() NOT NULL,
    kd_prop character(2),
    kd_dati2 character(2),
    kd_kec character(3),
    nama character varying(100),
    color character varying(30),
    geom text,
    created_at timestamp(0) without time zone,
    created_by character(36),
    updated_at timestamp without time zone,
    updated_by character(36) DEFAULT NULL::bpchar,
    deleted_at timestamp without time zone,
    deleted_by character(36),
    recover_at timestamp without time zone,
    recover_by character(36) DEFAULT NULL::bpchar,
    is_active boolean DEFAULT true
);


ALTER TABLE system.kecamatan OWNER TO postgres;

--
-- Name: kelurahan; Type: TABLE; Schema: system; Owner: postgres
--

CREATE TABLE system.kelurahan (
    id character(36) DEFAULT public.uuid_generate_v4() NOT NULL,
    kd_prop character(2),
    kd_dati2 character(2),
    kd_kec character(3),
    kd_kel character(3),
    nama character varying(100),
    geom text,
    created_at timestamp(0) without time zone,
    created_by character(36),
    updated_at timestamp without time zone,
    updated_by character(36) DEFAULT NULL::bpchar,
    deleted_at timestamp without time zone,
    deleted_by character(36),
    recover_at timestamp without time zone,
    recover_by character(36) DEFAULT NULL::bpchar,
    is_active boolean DEFAULT true
);


ALTER TABLE system.kelurahan OWNER TO postgres;

--
-- Name: pegawai; Type: TABLE; Schema: system; Owner: postgres
--

CREATE TABLE system.pegawai (
    id character(36) DEFAULT public.uuid_generate_v4() NOT NULL,
    nama character varying(100),
    jabatan character varying(100),
    nip character varying(30),
    golongan character varying(30),
    pangkat character varying(30),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character(36) DEFAULT NULL::bpchar,
    updated_at timestamp without time zone,
    updated_by character(36) DEFAULT NULL::bpchar,
    deleted_at timestamp without time zone,
    deleted_by character(36) DEFAULT NULL::bpchar,
    recover_at timestamp without time zone,
    recover_by character(36) DEFAULT NULL::bpchar,
    is_active boolean DEFAULT true
);


ALTER TABLE system.pegawai OWNER TO postgres;

--
-- Name: pemda; Type: TABLE; Schema: system; Owner: postgres
--

CREATE TABLE system.pemda (
    id integer DEFAULT 1 NOT NULL,
    nm_prop character varying(50),
    nm_dati2 character varying(50),
    nm_ibukota character varying(100),
    kd_prop character(2),
    kd_dati2 character(3),
    nm_full_instansi character varying(100),
    nm_singkat_instansi character varying(30),
    alamat character varying(255),
    telp character varying(30),
    fax character varying(30),
    website character varying(255),
    email character varying(255),
    kodepos character(5),
    latitude double precision,
    longitude double precision,
    zoom smallint,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_by integer,
    updated_at timestamp without time zone,
    updated_by integer,
    deleted_at timestamp without time zone,
    deleted_by integer,
    recover_at timestamp without time zone,
    recover_by integer,
    is_active boolean DEFAULT true
);


ALTER TABLE system.pemda OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: system; Owner: postgres
--

CREATE TABLE system."user" (
    id character(36) DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(32),
    password character varying(64),
    nama character varying(100),
    id_unit character(36) DEFAULT NULL::bpchar,
    token character(64),
    is_admin boolean DEFAULT false,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    created_by character(36) DEFAULT NULL::bpchar,
    updated_at timestamp without time zone,
    updated_by character(36) DEFAULT NULL::bpchar,
    deleted_at timestamp without time zone,
    deleted_by character(36) DEFAULT NULL::bpchar,
    recover_at timestamp without time zone,
    recover_by character(36) DEFAULT NULL::bpchar,
    is_active boolean DEFAULT true,
    role character varying(25)
);


ALTER TABLE system."user" OWNER TO postgres;

--
-- Name: desa_kepuharjo id; Type: DEFAULT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.desa_kepuharjo ALTER COLUMN id SET DEFAULT nextval('system.desa_kepuharjo_id_seq'::regclass);


--
-- Name: bidang bidang_pkey; Type: CONSTRAINT; Schema: sig; Owner: postgres
--

ALTER TABLE ONLY sig.bidang
    ADD CONSTRAINT bidang_pkey PRIMARY KEY (id);


--
-- Name: blok blok_pkey; Type: CONSTRAINT; Schema: sig; Owner: postgres
--

ALTER TABLE ONLY sig.blok
    ADD CONSTRAINT blok_pkey PRIMARY KEY (id);


--
-- Name: blok blok_ukey; Type: CONSTRAINT; Schema: sig; Owner: postgres
--

ALTER TABLE ONLY sig.blok
    ADD CONSTRAINT blok_ukey UNIQUE (kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok);


--
-- Name: bidang nop_ukey; Type: CONSTRAINT; Schema: sig; Owner: postgres
--

ALTER TABLE ONLY sig.bidang
    ADD CONSTRAINT nop_ukey UNIQUE (kd_prop, kd_dati2, kd_kec, kd_kel, kd_blok, no_urut, kd_jns_op);


--
-- Name: desa_kepuharjo desa_kepuharjo_pkey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.desa_kepuharjo
    ADD CONSTRAINT desa_kepuharjo_pkey PRIMARY KEY (id);


--
-- Name: kecamatan kecamatan_pkey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.kecamatan
    ADD CONSTRAINT kecamatan_pkey PRIMARY KEY (id);


--
-- Name: kecamatan kecamatan_ukey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.kecamatan
    ADD CONSTRAINT kecamatan_ukey UNIQUE (kd_prop, kd_dati2, kd_kec, nama);


--
-- Name: kelurahan kelurahan_pkey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.kelurahan
    ADD CONSTRAINT kelurahan_pkey PRIMARY KEY (id);


--
-- Name: kelurahan kelurahan_ukey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.kelurahan
    ADD CONSTRAINT kelurahan_ukey UNIQUE (kd_prop, kd_dati2, kd_kec, kd_kel);


--
-- Name: pegawai pegawai_pkey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.pegawai
    ADD CONSTRAINT pegawai_pkey PRIMARY KEY (id);


--
-- Name: pemda pemda_pkey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system.pemda
    ADD CONSTRAINT pemda_pkey PRIMARY KEY (id);


--
-- Name: user user_pkey; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: user user_username_key; Type: CONSTRAINT; Schema: system; Owner: postgres
--

ALTER TABLE ONLY system."user"
    ADD CONSTRAINT user_username_key UNIQUE (username);


--
-- Name: bidang_nop_idx; Type: INDEX; Schema: sig; Owner: postgres
--

CREATE UNIQUE INDEX bidang_nop_idx ON sig.bidang USING btree (nop);


--
-- PostgreSQL database dump complete
--

\unrestrict I1AJ6EDDBza42ddfHN15JdrO9Tp3zuqqj9Qpj5WJFrpoDGVul9pkxVIJOg32bug

