-- Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
-- --------------------------------------------------------------------------------
-- Tool Version: Vivado v.2018.3 (win64) Build 2405991 Thu Dec  6 23:38:27 MST 2018
-- Date        : Mon Oct 13 22:42:29 2025
-- Host        : anbera running 64-bit major release  (build 9200)
-- Command     : write_vhdl -force -mode synth_stub {c:/Users/yanha/Desktop/Vivado
--               project/project_1_3_mux4x1.block_design/project_1_3_mux4x1.block_design.srcs/sources_1/bd/mux4x1_block/ip/mux4x1_block_notgate_2_0_0/mux4x1_block_notgate_2_0_0_stub.vhdl}
-- Design      : mux4x1_block_notgate_2_0_0
-- Purpose     : Stub declaration of top-level module interface
-- Device      : xc7a35tcpg236-1
-- --------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity mux4x1_block_notgate_2_0_0 is
  Port ( 
    in1 : in STD_LOGIC;
    in2 : in STD_LOGIC;
    out1 : out STD_LOGIC;
    out2 : out STD_LOGIC
  );

end mux4x1_block_notgate_2_0_0;

architecture stub of mux4x1_block_notgate_2_0_0 is
attribute syn_black_box : boolean;
attribute black_box_pad_pin : string;
attribute syn_black_box of stub : architecture is true;
attribute black_box_pad_pin of stub : architecture is "in1,in2,out1,out2";
attribute X_CORE_INFO : string;
attribute X_CORE_INFO of stub : architecture is "notgate_2,Vivado 2018.3";
begin
end;
