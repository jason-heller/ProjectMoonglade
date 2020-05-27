#version 150

in vec3 in_position;
in vec2 in_textureCoords;

out vec2 pass_textureCoords;
out vec4 clipSpace;

uniform mat4 projectionViewMatrix;
uniform vec4 offset;

void main(void){

	clipSpace = projectionViewMatrix * vec4((in_position.x+offset.x)*offset.z, (offset.w), (in_position.z+offset.y)*offset.z, 1.0);
	gl_Position = clipSpace;
	
	pass_textureCoords = vec2(in_textureCoords.x/2.0 + 0.5, in_textureCoords.y/2.0 + 0.5) * 3.0;
}
