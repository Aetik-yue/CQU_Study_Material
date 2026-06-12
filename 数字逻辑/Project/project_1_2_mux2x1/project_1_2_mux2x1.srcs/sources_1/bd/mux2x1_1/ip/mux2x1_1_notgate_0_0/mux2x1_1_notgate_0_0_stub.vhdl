-- Copyright 1986-2018 Xilinx, Inc. All Rights Reserved.
-- --------------------------------------------------------------------------------
-- Tool Version: Vivado v.2018.3 (win64) Build 2405991 Thu Dec  6 23:38:27 MST 2018
-- Date        : Mon Oct 13 20:39:02 2025
-- Host        : anbera running 64-bit major release  (build 9200)
-- Command     : write_vhdl -force -mode synth_stub {c:/Users/yanha/Desktop/Vivado
--               project/project_1_2_mux2x1/project_1_2_mux2x1.srcs/sources_1/bd/mux2x1_1/ip/mux2x1_1_notgate_0_0/mux2x1_1_notgate_0_0_stub.vhdl}
-- Design      : mux2x1_1_notgate_0_0
-- Purpose     : Stub declaration of top-level module interface
-- Device      : xc7a35tcpg236-1
-- --------------------------------------------------------------------------------
library IEEE;
use IEEE.STD_LOGIC_1164.ALL;

entity mux2x1_1_notgate_0_0 is
  Port ( 
    a : in STD_LOGIC_VECTOR ( 0 to 0 );
    y : out STD_LOGIC_VECTOR ( 0 to 0 )
  );

end mux2x1_1_notgate_0_0;

architecture stub of mux2x1_1_notgate_0_0 is
attribute syn_black_box : boolean;
attribute black_box_pad_pin : string;
attribute syn_black_box of stub : architecture is true;
attribute black_box_pad_pin of stub : architecture is "a[0:0],y[0:0]";
attribute X_CORE_INFO : string;
attribute X_CORE_INFO of stub : architecture is "notgate,Vivado 2018.3";
begin
end;
