import { render, screen } from '@testing-library/react';
import App from './App';

test('renders the asset allocation console', () => {
  render(<App />);
  expect(screen.getByText(/Asset Allocation Module/i)).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Assign Asset/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Return Asset/i })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: /Transfer Asset/i })).toBeInTheDocument();
});
