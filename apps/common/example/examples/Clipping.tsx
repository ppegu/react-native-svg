import React from 'react';
import {
  Circle,
  ClipPath,
  Defs,
  Ellipse,
  G,
  Path,
  Polygon,
  RadialGradient,
  Rect,
  Stop,
  Svg,
  Text,
} from 'react-native-svg';

function ClipPathElement() {
  return (
    <Svg height="100" width="100">
      <Defs>
        <RadialGradient
          id="clip-path-grad"
          cx="50%"
          cy="50%"
          rx="50%"
          ry="50%"
          fx="50%"
          fy="50%">
          <Stop offset="0%" stopColor="#ff0" stopOpacity="1" />
          <Stop offset="100%" stopColor="#00f" stopOpacity="1" />
        </RadialGradient>
        <ClipPath id="clip-path-clip">
          <Circle cx="30" cy="30" r="20" />
          <Ellipse cx="60" cy="70" rx="20" ry="10" />
          <Rect x="65" y="15" width="30" height="30" />
          <Polygon points="20,60 20,80 50,70" />
          <Text
            x="50"
            y="30"
            fontSize="32"
            fontWeight="bold"
            textAnchor="middle"
            scale="1.2">
            Q
          </Text>
        </ClipPath>
      </Defs>
      <Rect
        x="0"
        y="0"
        width="100"
        height="100"
        fill="url(#clip-path-grad)"
        clipPath="url(#clip-path-clip)"
      />
    </Svg>
  );
}
ClipPathElement.title = 'Clip by set clip-path with a path data';

function ClipRule() {
  return (
    <Svg height="100" width="100">
      <Defs>
        <ClipPath id="clip-rule-clip">
          <Path d="M50,5L20,99L95,39L5,39L80,99z" />
        </ClipPath>
      </Defs>
      <G clipPath="url(#clip-rule-clip)" clipRule="evenodd">
        <Rect x="0" y="0" width="50" height="50" fill="red" />
        <Rect x="50" y="0" width="50" height="50" fill="blue" />
        <Rect x="0" y="50" width="50" height="50" fill="yellow" />
        <Rect x="50" y="50" width="50" height="50" fill="green" />
      </G>
    </Svg>
  );
}
ClipRule.title = 'Clip a group with clipRule="evenodd"';

function TextClipping() {
  return (
    <Svg height="60" width="200">
      <Defs>
        <ClipPath id="text-clipping-clip">
          <Circle cx="-20" cy="35" r="10" />
          <Circle cx="0" cy="35" r="10" />
          <Circle cx="20" cy="35" r="10" />
          <Circle cx="40" cy="35" r="10" />
          <Circle cx="60" cy="35" r="10" />
          <Circle cx="80" cy="35" r="10" />
          <Circle cx="100" cy="35" r="10" />
          <Circle cx="120" cy="35" r="10" />
          <Circle cx="140" cy="35" r="10" />
          <Circle cx="160" cy="35" r="10" />
          <Circle cx="180" cy="35" r="10" />
        </ClipPath>
      </Defs>
      <Text
        x="100"
        y="40"
        fill="red"
        fontSize="22"
        fontWeight="bold"
        stroke="blue"
        strokeWidth="1"
        textAnchor="middle"
        clipPath="url(#text-clipping-clip)">
        NOT THE FACE
      </Text>
    </Svg>
  );
}
TextClipping.title = 'Transform the text';

const icon = (
  <Svg height="30" width="30" viewBox="0 0 20 20">
    <Defs>
      <ClipPath id="clipping-icon-clip">
        <Path d="M50,5L20,99L95,39L5,39L80,99z" />
      </ClipPath>
    </Defs>
    <G clipPath="url(#clipping-icon-clip)" clipRule="evenodd" scale="0.2">
      <Rect x="0" y="0" width="50" height="50" fill="red" />
      <Rect x="50" y="0" width="50" height="50" fill="blue" />
      <Rect x="0" y="50" width="50" height="50" fill="yellow" />
      <Rect x="50" y="50" width="50" height="50" fill="green" />
    </G>
  </Svg>
);
const samples = [ClipPathElement, ClipRule, TextClipping];

export {icon, samples};
