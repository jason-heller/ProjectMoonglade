#version 150

in vec3 col;
out vec4 out_colour;

void main(void){
	out_colour = vec4(col, 1);
}
