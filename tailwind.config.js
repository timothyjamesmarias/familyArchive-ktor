/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class', // Enable class-based dark mode
  content: ['./src/main/frontend/**/*.{js,ts,jsx,tsx}', './src/main/kotlin/**/*.kt'],
  // Safelist common utility patterns to ensure they're always included
  safelist: [
    {
      pattern:
        /^(bg|text|border|ring)-(stone|sage|terracotta|dusty|taupe)-(50|100|200|300|400|500|600|700|800|900)$/,
    },
    {
      pattern: /^(p|m|px|py|mx|my|mt|mb|ml|mr|pt|pb|pl|pr)-[0-9]+$/,
    },
  ],
  theme: {
    extend: {
      // Custom color palettes for paperwhite design system
      colors: {
        // Sage - Muted Green
        sage: {
          50: '#f6f7f6',
          100: '#e3e7e3',
          200: '#c7cfc7',
          300: '#a3b0a3',
          400: '#7a8c7a',
          500: '#5d6f5d',
          600: '#4a5a4a',
          700: '#3d483d',
          800: '#343b34',
          900: '#2d322d',
        },
        // Terracotta - Muted Orange-Red
        terracotta: {
          50: '#faf6f5',
          100: '#f3e9e6',
          200: '#e7d2cc',
          300: '#d6b3a8',
          400: '#c18d7c',
          500: '#a66f5d',
          600: '#8c5c4d',
          700: '#744c41',
          800: '#604039',
          900: '#513832',
        },
        // Dusty Blue - Muted Blue
        dusty: {
          50: '#f5f7f8',
          100: '#e7ecee',
          200: '#d3dde1',
          300: '#b3c4cb',
          400: '#8ca3ae',
          500: '#6b8594',
          600: '#596d7c',
          700: '#4b5b66',
          800: '#414d56',
          900: '#3a4349',
        },
        // Taupe - Warm Gray-Brown
        taupe: {
          50: '#f8f7f6',
          100: '#efedeb',
          200: '#dbd7d3',
          300: '#c1b9b3',
          400: '#a3968d',
          500: '#8a7c72',
          600: '#736861',
          700: '#5f5550',
          800: '#504845',
          900: '#463e3c',
        },
        // Functional colors (muted versions)
        success: {
          light: { bg: '#e8f3e8', text: '#4a7c4a' },
          dark: { bg: '#2d3e2d', text: '#7fb07f' },
        },
        error: {
          light: { bg: '#f5e8e8', text: '#8c4a4a' },
          dark: { bg: '#3e2d2d', text: '#c98080' },
        },
        warning: {
          light: { bg: '#f5f0e8', text: '#8c7a4a' },
          dark: { bg: '#3e3a2d', text: '#c9b380' },
        },
        info: {
          light: { bg: '#e8eff5', text: '#4a6b8c' },
          dark: { bg: '#2d3640', text: '#809fc9' },
        },
      },
      // Typography
      fontFamily: {
        serif: ['Lora', 'Georgia', 'Times New Roman', 'serif'],
        sans: [
          'Inter',
          'Open Sans',
          '-apple-system',
          'BlinkMacSystemFont',
          'Segoe UI',
          'sans-serif',
        ],
      },
      // Line height adjustments for better readability
      lineHeight: {
        'extra-relaxed': '1.75',
        'super-relaxed': '1.8',
      },
      // Base font size
      fontSize: {
        base: '1rem', // 16px
        lg: '1.125rem', // 18px - new default for body
        xl: '1.25rem', // 20px - for article body
        '2xl': '1.5rem', // 24px
        '3xl': '1.875rem', // 30px
        '4xl': '2.25rem', // 36px
      },
    },
  },
  plugins: [
    // Typography plugin for article content (install with: npm install @tailwindcss/typography)
    // require('@tailwindcss/typography'),
  ],
};
