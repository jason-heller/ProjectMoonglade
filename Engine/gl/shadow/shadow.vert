#version 150

in vec3 in_position;
in vec2 in_uvs;

out vec2 pass_uvs;

uniform mat4 projViewModelMatrix;

void main(void){
	pass_uvs = in_uvs;
	gl_Position = projViewModelMatrix * vec4(in_position, 1.0);

}